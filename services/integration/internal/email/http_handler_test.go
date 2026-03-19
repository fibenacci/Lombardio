package email

import (
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestHandlerSendsEmailWithInternalToken(t *testing.T) {
	sender := &fakeSender{}
	handler := NewHandler("token-123", sender)

	payload, _ := json.Marshal(Message{
		TenantID: "tenant-default",
		To:       []string{"anna@example.test"},
		Subject:  "Portal",
		TextBody: "Activation",
	})

	request := httptest.NewRequest(http.MethodPost, "/internal/v1/emails/send", bytes.NewReader(payload))
	request.Header.Set("X-Internal-Service-Token", "token-123")
	response := httptest.NewRecorder()

	handler.ServeHTTP(response, request)

	if response.Code != http.StatusAccepted {
		t.Fatalf("expected status %d, got %d", http.StatusAccepted, response.Code)
	}
	if sender.lastMessage.Subject != "Portal" {
		t.Fatalf("expected email subject to be recorded, got %#v", sender.lastMessage)
	}
}

type fakeSender struct {
	lastMessage Message
}

func (f *fakeSender) Send(_ context.Context, message Message) error {
	f.lastMessage = message
	return nil
}
