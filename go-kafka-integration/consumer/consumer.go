package consumer

import (
	"context"
	"encoding/json"
	"go-kafka-integration/config"
	"go-kafka-integration/models"
	"log"
	"os"
	"os/signal"
	"sync"
	"syscall"

	"github.com/IBM/sarama"
)

// NotificationHandler interface for handling notifications
type NotificationHandler interface {
	Handle(notification models.Notification)
}

// KafkaConsumer represents a Kafka consumer
type KafkaConsumer struct {
	client     sarama.ConsumerGroup
	handlers   map[string]NotificationHandler
	config     *config.Config
	ready      chan bool
	cancelFunc context.CancelFunc
	wg         sync.WaitGroup
}

// NewKafkaConsumer creates a new Kafka consumer
func NewKafkaConsumer(cfg *config.Config) (*KafkaConsumer, error) {
	// Configure Sarama (Kafka client)
	saramaConfig := sarama.NewConfig()
	saramaConfig.Consumer.Group.Rebalance.Strategy = sarama.BalanceStrategyRoundRobin
	saramaConfig.Consumer.Offsets.Initial = sarama.OffsetOldest

	// Create consumer group
	client, err := sarama.NewConsumerGroup(cfg.KafkaBrokers, cfg.KafkaGroupID, saramaConfig)
	if err != nil {
		return nil, err
	}

	consumer := &KafkaConsumer{
		client:   client,
		handlers: make(map[string]NotificationHandler),
		config:   cfg,
		ready:    make(chan bool),
	}

	return consumer, nil
}

// RegisterHandler registers a handler for a specific notification type
func (c *KafkaConsumer) RegisterHandler(notificationType string, handler NotificationHandler) {
	c.handlers[notificationType] = handler
}

// Start starts consuming messages from Kafka
func (c *KafkaConsumer) Start() error {
	// Create a context with cancellation
	ctx, cancel := context.WithCancel(context.Background())
	c.cancelFunc = cancel

	// Listen for shutdown signals
	signals := make(chan os.Signal, 1)
	signal.Notify(signals, syscall.SIGINT, syscall.SIGTERM)

	// Topics to consume
	topics := []string{
		c.config.NotificationTopic,
		c.config.EmailTopic,
		c.config.SmsTopic,
		c.config.PushTopic,
	}

	// Start consuming in a goroutine
	c.wg.Add(1)
	go func() {
		defer c.wg.Done()
		for {
			// Consume from topics
			if err := c.client.Consume(ctx, topics, c); err != nil {
				log.Printf("Error from consumer: %v", err)
			}
			// Check if context was cancelled
			if ctx.Err() != nil {
				return
			}
			c.ready = make(chan bool)
		}
	}()

	// Wait for consumer to be ready
	<-c.ready
	log.Println("Kafka consumer started")

	// Wait for shutdown signal
	select {
	case <-signals:
		log.Println("Received shutdown signal, stopping consumer...")
		cancel()
	case <-ctx.Done():
		log.Println("Context cancelled, stopping consumer...")
	}

	c.wg.Wait()
	if err := c.client.Close(); err != nil {
		log.Printf("Error closing client: %v", err)
		return err
	}

	return nil
}

// Stop stops the consumer
func (c *KafkaConsumer) Stop() {
	if c.cancelFunc != nil {
		c.cancelFunc()
		c.wg.Wait()
	}
}

// Setup is called when the consumer starts
func (c *KafkaConsumer) Setup(session sarama.ConsumerGroupSession) error {
	close(c.ready)
	return nil
}

// Cleanup is called when the consumer stops
func (c *KafkaConsumer) Cleanup(sarama.ConsumerGroupSession) error {
	return nil
}

// ConsumeClaim is called for each topic/partition being consumed
func (c *KafkaConsumer) ConsumeClaim(session sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	for message := range claim.Messages() {
		log.Printf("Received message from topic %s, partition %d at offset %d",
			message.Topic, message.Partition, message.Offset)

		messageValue := string(message.Value)
		log.Printf("Message value: %s", messageValue)

		// Parse notification
		var notification models.Notification
		if err := json.Unmarshal(message.Value, &notification); err != nil {
			log.Printf("Error unmarshalling notification: %v", err)
			session.MarkMessage(message, "")
			continue
		}

		// Determine notification type based on topic or message content
		var notificationType string
		switch message.Topic {
		case c.config.EmailTopic:
			notificationType = models.EMAIL
		case c.config.SmsTopic:
			notificationType = models.SMS
		case c.config.PushTopic:
			notificationType = models.PUSH
		default:
			notificationType = notification.Type
		}

		// Process notification with appropriate handler
		if handler, ok := c.handlers[notificationType]; ok {
			handler.Handle(notification)
		} else {
			log.Printf("No handler registered for notification type: %s", notificationType)
		}

		// Mark message as processed
		session.MarkMessage(message, "")
	}
	return nil
}