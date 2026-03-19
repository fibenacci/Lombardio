package app

import (
	"context"
	"encoding/json"
	"testing"
	"time"

	"io.lombardio/integration-service/internal/events"
)

func TestProcessEventDispatchesToWebhookDispatcher(t *testing.T) {
	t.Helper()

	dispatcher := &fakeDispatcher{}
	app := New(dispatcher)

	event := events.Event{
		ID:            "outbox-1",
		AggregateType: "tenant",
		AggregateID:   "tenant-default",
		EventType:     "platform.tenant.created",
		TenantID:      "tenant-default",
		OccurredAt:    time.Date(2026, 3, 19, 0, 0, 0, 0, time.UTC),
		Payload:       json.RawMessage(`{"tenantId":"tenant-default"}`),
	}

	if err := app.ProcessEvent(context.Background(), event); err != nil {
		t.Fatalf("ProcessEvent returned error: %v", err)
	}

	if len(dispatcher.dispatchedIDs) != 1 || dispatcher.dispatchedIDs[0] != "outbox-1" {
		t.Fatalf("expected one dispatched event outbox-1, got %#v", dispatcher.dispatchedIDs)
	}
}

type fakeDispatcher struct {
	dispatchedIDs []string
}

func (f *fakeDispatcher) Dispatch(_ context.Context, event events.Event) error {
	f.dispatchedIDs = append(f.dispatchedIDs, event.ID)
	return nil
}
