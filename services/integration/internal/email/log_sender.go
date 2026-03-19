package email

import (
	"context"
	"log"
)

type LogSender struct{}

func NewLogSender() *LogSender {
	return &LogSender{}
}

func (s *LogSender) Send(_ context.Context, message Message) error {
	log.Printf(
		"email prepared tenant=%s to=%v subject=%q metadata=%v text=%q",
		message.TenantID,
		message.To,
		message.Subject,
		message.Metadata,
		message.TextBody,
	)
	return nil
}
