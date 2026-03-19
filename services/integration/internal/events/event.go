package events

import (
	"encoding/json"
	"time"
)

type Event struct {
	ID            string          `json:"id"`
	AggregateType string          `json:"aggregateType"`
	AggregateID   string          `json:"aggregateId"`
	EventType     string          `json:"eventType"`
	TenantID      string          `json:"tenantId"`
	OccurredAt    time.Time       `json:"occurredAt"`
	Payload       json.RawMessage `json:"payload"`
}
