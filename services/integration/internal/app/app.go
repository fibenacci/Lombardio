package app

import (
	"context"

	"io.lombardio/integration-service/internal/events"
	"io.lombardio/integration-service/internal/webhook"
)

type App struct {
	dispatcher webhook.Dispatcher
}

func New(dispatcher webhook.Dispatcher) *App {
	return &App{dispatcher: dispatcher}
}

func (a *App) ProcessEvent(ctx context.Context, event events.Event) error {
	return a.dispatcher.Dispatch(ctx, event)
}
