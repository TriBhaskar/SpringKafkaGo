package main

import (
	"go-kafka-integration/config"
	"go-kafka-integration/consumer"
	"go-kafka-integration/handlers"
	"go-kafka-integration/models"
	"log"
)

func main() {
	// Load configuration
	cfg := config.LoadConfig()
	
	// Create Kafka consumer
	kafkaConsumer, err := consumer.NewKafkaConsumer(cfg)
	if err != nil {
		log.Fatalf("Failed to create Kafka consumer: %v", err)
	}
	
	// Register notification handlers
	kafkaConsumer.RegisterHandler(models.EMAIL, handlers.NewEmailHandler())
	kafkaConsumer.RegisterHandler(models.SMS, handlers.NewSmsHandler())
	kafkaConsumer.RegisterHandler(models.PUSH, handlers.NewPushHandler())
	
	// Log that we're starting
	log.Println("Starting notification consumer service...")
	log.Printf("Connected to Kafka brokers: %v", cfg.KafkaBrokers)
	log.Printf("Consuming from topics: %s, %s, %s, %s", 
		cfg.NotificationTopic,
		cfg.EmailTopic,
		cfg.SmsTopic,
		cfg.PushTopic)
	
	// Start consuming messages
	if err := kafkaConsumer.Start(); err != nil {
		log.Fatalf("Failed to start Kafka consumer: %v", err)
	}
	
	log.Println("Notification consumer service stopped")
}