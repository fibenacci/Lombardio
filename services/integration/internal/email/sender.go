package email

import "context"

type Message struct {
	TenantID  string            `json:"tenantId"`
	To        []string          `json:"to"`
	ReplyTo   []string          `json:"replyTo,omitempty"`
	Subject   string            `json:"subject"`
	TextBody  string            `json:"textBody"`
	HTMLBody  string            `json:"htmlBody,omitempty"`
	Metadata  map[string]string `json:"metadata,omitempty"`
}

type Sender interface {
	Send(ctx context.Context, message Message) error
}
