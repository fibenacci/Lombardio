package config

import (
	"encoding/json"
	"fmt"
	"os"
	"strings"
)

type WebhookDestination struct {
	Name       string   `json:"name"`
	URL        string   `json:"url"`
	EventTypes []string `json:"eventTypes"`
}

type Config struct {
	Port                string
	RabbitMQURL         string
	RabbitMQExchange    string
	RabbitMQQueue       string
	RabbitMQBindingKey  string
	WebhookDestinations []WebhookDestination
	InternalServiceToken string
	EmailDeliveryMode    string
	SMTPAddress          string
	SMTPUsername         string
	SMTPPassword         string
	SMTPFromAddress      string
	SMTPFromName         string
}

func LoadFromEnv() (Config, error) {
	cfg := Config{
		Port:               strings.TrimSpace(os.Getenv("PORT")),
		RabbitMQURL:        strings.TrimSpace(os.Getenv("RABBITMQ_URL")),
		RabbitMQExchange:   strings.TrimSpace(os.Getenv("RABBITMQ_EXCHANGE")),
		RabbitMQQueue:      strings.TrimSpace(os.Getenv("RABBITMQ_QUEUE")),
		RabbitMQBindingKey: strings.TrimSpace(os.Getenv("RABBITMQ_BINDING_KEY")),
		InternalServiceToken: strings.TrimSpace(os.Getenv("INTEGRATION_INTERNAL_SERVICE_TOKEN")),
		EmailDeliveryMode:    strings.TrimSpace(os.Getenv("INTEGRATION_EMAIL_DELIVERY_MODE")),
		SMTPAddress:          strings.TrimSpace(os.Getenv("INTEGRATION_SMTP_ADDRESS")),
		SMTPUsername:         strings.TrimSpace(os.Getenv("INTEGRATION_SMTP_USERNAME")),
		SMTPPassword:         strings.TrimSpace(os.Getenv("INTEGRATION_SMTP_PASSWORD")),
		SMTPFromAddress:      strings.TrimSpace(os.Getenv("INTEGRATION_SMTP_FROM_ADDRESS")),
		SMTPFromName:         strings.TrimSpace(os.Getenv("INTEGRATION_SMTP_FROM_NAME")),
	}

	if cfg.Port == "" {
		return Config{}, fmt.Errorf("PORT is required")
	}

	if cfg.RabbitMQURL == "" {
		return Config{}, fmt.Errorf("RABBITMQ_URL is required")
	}
	if cfg.RabbitMQExchange == "" {
		return Config{}, fmt.Errorf("RABBITMQ_EXCHANGE is required")
	}
	if cfg.RabbitMQQueue == "" {
		return Config{}, fmt.Errorf("RABBITMQ_QUEUE is required")
	}
	if cfg.RabbitMQBindingKey == "" {
		return Config{}, fmt.Errorf("RABBITMQ_BINDING_KEY is required")
	}
	if cfg.InternalServiceToken == "" {
		return Config{}, fmt.Errorf("INTEGRATION_INTERNAL_SERVICE_TOKEN is required")
	}
	if cfg.EmailDeliveryMode == "" {
		cfg.EmailDeliveryMode = "log"
	}
	if cfg.EmailDeliveryMode != "log" && cfg.EmailDeliveryMode != "smtp" {
		return Config{}, fmt.Errorf("INTEGRATION_EMAIL_DELIVERY_MODE must be log or smtp")
	}
	if cfg.EmailDeliveryMode == "smtp" {
		if cfg.SMTPAddress == "" {
			return Config{}, fmt.Errorf("INTEGRATION_SMTP_ADDRESS is required for smtp mode")
		}
		if cfg.SMTPFromAddress == "" {
			return Config{}, fmt.Errorf("INTEGRATION_SMTP_FROM_ADDRESS is required for smtp mode")
		}
	}

	rawDestinations := strings.TrimSpace(os.Getenv("INTEGRATION_WEBHOOK_DESTINATIONS"))
	if rawDestinations == "" {
		return Config{}, fmt.Errorf("INTEGRATION_WEBHOOK_DESTINATIONS is required")
	}
	if err := json.Unmarshal([]byte(rawDestinations), &cfg.WebhookDestinations); err != nil {
		return Config{}, fmt.Errorf("parse INTEGRATION_WEBHOOK_DESTINATIONS: %w", err)
	}

	return cfg, nil
}
