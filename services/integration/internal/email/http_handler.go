package email

import (
	"encoding/json"
	"errors"
	"net/http"
	"strings"
)

type Handler struct {
	internalServiceToken string
	sender               Sender
}

func NewHandler(internalServiceToken string, sender Sender) *Handler {
	return &Handler{
		internalServiceToken: internalServiceToken,
		sender:               sender,
	}
}

func (h *Handler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}
	if strings.TrimSpace(r.Header.Get("X-Internal-Service-Token")) != h.internalServiceToken {
		http.Error(w, "forbidden", http.StatusForbidden)
		return
	}

	var message Message
	if err := json.NewDecoder(r.Body).Decode(&message); err != nil {
		http.Error(w, "invalid json body", http.StatusBadRequest)
		return
	}
	if err := validateMessage(message); err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}
	if err := h.sender.Send(r.Context(), message); err != nil {
		http.Error(w, "email delivery failed", http.StatusBadGateway)
		return
	}

	w.WriteHeader(http.StatusAccepted)
}

func validateMessage(message Message) error {
	if strings.TrimSpace(message.Subject) == "" {
		return errors.New("subject is required")
	}
	if strings.TrimSpace(message.TextBody) == "" {
		return errors.New("textBody is required")
	}
	if len(message.To) == 0 {
		return errors.New("at least one recipient is required")
	}
	return nil
}
