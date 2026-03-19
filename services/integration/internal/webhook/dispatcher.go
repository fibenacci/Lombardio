package webhook

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"net/http"

	"io.lombardio/integration-service/internal/config"
	"io.lombardio/integration-service/internal/events"
)

type Dispatcher interface {
	Dispatch(ctx context.Context, event events.Event) error
}

type HTTPDispatcher struct {
	destinations []config.WebhookDestination
	httpClient   *http.Client
}

type deliveryEnvelope struct {
	ID            string          `json:"id"`
	AggregateType string          `json:"aggregateType"`
	AggregateID   string          `json:"aggregateId"`
	EventType     string          `json:"eventType"`
	TenantID      string          `json:"tenantId"`
	OccurredAt    string          `json:"occurredAt"`
	Payload       json.RawMessage `json:"payload"`
}

func NewHTTPDispatcher(destinations []config.WebhookDestination, httpClient *http.Client) *HTTPDispatcher {
	return &HTTPDispatcher{
		destinations: destinations,
		httpClient:   httpClient,
	}
}

func (d *HTTPDispatcher) Dispatch(ctx context.Context, event events.Event) error {
	matched := false

	for _, destination := range d.destinations {
		if !handles(destination, event.EventType) {
			continue
		}
		matched = true

		payload, err := json.Marshal(deliveryEnvelope{
			ID:            event.ID,
			AggregateType: event.AggregateType,
			AggregateID:   event.AggregateID,
			EventType:     event.EventType,
			TenantID:      event.TenantID,
			OccurredAt:    event.OccurredAt.UTC().Format("2006-01-02T15:04:05Z"),
			Payload:       event.Payload,
		})
		if err != nil {
			return fmt.Errorf("marshal webhook payload: %w", err)
		}

		req, err := http.NewRequestWithContext(ctx, http.MethodPost, destination.URL, bytes.NewReader(payload))
		if err != nil {
			return fmt.Errorf("build webhook request for %s: %w", destination.Name, err)
		}
		req.Header.Set("Content-Type", "application/json")

		resp, err := d.httpClient.Do(req)
		if err != nil {
			return fmt.Errorf("deliver to %s: %w", destination.Name, err)
		}
		resp.Body.Close()

		if resp.StatusCode < 200 || resp.StatusCode >= 300 {
			return fmt.Errorf("deliver to %s returned %s", destination.Name, resp.Status)
		}
	}

	if !matched {
		return nil
	}
	return nil
}

func handles(destination config.WebhookDestination, eventType string) bool {
	for _, supported := range destination.EventTypes {
		if supported == eventType {
			return true
		}
	}
	return false
}
