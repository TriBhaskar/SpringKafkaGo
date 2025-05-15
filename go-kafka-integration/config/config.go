package config

import (
	"log"
	"os"
	"strings"

	"github.com/joho/godotenv"
)

// Config holds all configuration for our application
type Config struct {
	KafkaBrokers     []string
	KafkaGroupID     string
	NotificationTopic string
	EmailTopic       string
	SmsTopic         string
	PushTopic        string
	LogLevel         string
}

// LoadConfig loads configuration from environment variables or .env file
func LoadConfig() *Config {
	// Load .env file if it exists
	err := godotenv.Load()
	if err != nil {
		log.Println("Warning: .env file not found, using environment variables")
	}

	// Get configuration from environment variables
	kafkaBrokers := strings.Split(getEnv("KAFKA_BROKERS", "localhost:9092"), ",")
	
	return &Config{
		KafkaBrokers:     kafkaBrokers,
		KafkaGroupID:     getEnv("KAFKA_GROUP_ID", "notification-go-consumer"),
		NotificationTopic: getEnv("NOTIFICATION_TOPIC", "notifications"),
		EmailTopic:       getEnv("EMAIL_TOPIC", "email-notifications"),
		SmsTopic:         getEnv("SMS_TOPIC", "sms-notifications"),
		PushTopic:        getEnv("PUSH_TOPIC", "push-notifications"),
		LogLevel:         getEnv("LOG_LEVEL", "info"),
	}
}

// Helper function to get environment variable with a default value
func getEnv(key, defaultValue string) string {
	value := os.Getenv(key)
	if value == "" {
		return defaultValue
	}
	return value
}