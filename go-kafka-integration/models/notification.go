package models

import (
	"time"
)

// Notification represents a notification message in the system
type Notification struct {
	ID        string    `json:"id"`
	Recipient string    `json:"recipient"`
	Subject   string    `json:"subject"`
	Content   string    `json:"content"`
	Type      string    `json:"type"` // EMAIL, SMS, PUSH
	Timestamp time.Time `json:"timestamp"`
	Read      bool      `json:"read"`
}

// NotificationType enum
const (
	EMAIL = "EMAIL"
	SMS   = "SMS"
	PUSH  = "PUSH"
)