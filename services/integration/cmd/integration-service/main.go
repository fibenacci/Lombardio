package main

import (
	"context"
	"errors"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"io.lombardio/integration-service/internal/app"
	"io.lombardio/integration-service/internal/broker"
	"io.lombardio/integration-service/internal/config"
	"io.lombardio/integration-service/internal/email"
	"io.lombardio/integration-service/internal/events"
	"io.lombardio/integration-service/internal/webhook"
)

func main() {
	cfg, err := config.LoadFromEnv()
	if err != nil {
		log.Fatalf("load config: %v", err)
	}

	httpClient := &http.Client{Timeout: 10 * time.Second}
	dispatcher := webhook.NewHTTPDispatcher(cfg.WebhookDestinations, httpClient)
	worker := app.New(dispatcher)
	emailSender := buildEmailSender(cfg)

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	server := &http.Server{
		Addr:              ":" + cfg.Port,
		Handler:           routes(cfg.InternalServiceToken, emailSender),
		ReadHeaderTimeout: 5 * time.Second,
	}

	go func() {
		runConsumerLoop(ctx, cfg, worker.ProcessEvent)
	}()

	go func() {
		log.Printf("integration-service listening on :%s", cfg.Port)
		if err := server.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			log.Fatalf("http server failed: %v", err)
		}
	}()

	<-ctx.Done()

	shutdownCtx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := server.Shutdown(shutdownCtx); err != nil {
		log.Printf("http shutdown failed: %v", err)
	}
}

func runConsumerLoop(ctx context.Context, cfg config.Config, handler func(context.Context, events.Event) error) {
	const reconnectDelay = 3 * time.Second

	for ctx.Err() == nil {
		consumer, err := broker.NewConsumer(cfg.RabbitMQURL, cfg.RabbitMQExchange, cfg.RabbitMQQueue, cfg.RabbitMQBindingKey)
		if err != nil {
			log.Printf("create rabbitmq consumer failed: %v", err)
			if !sleepContext(ctx, reconnectDelay) {
				return
			}
			continue
		}

		err = consumer.Consume(ctx, handler)
		if closeErr := consumer.Close(); closeErr != nil {
			log.Printf("rabbitmq consumer close failed: %v", closeErr)
		}

		if ctx.Err() != nil {
			return
		}
		if err != nil {
			log.Printf("rabbitmq consumer stopped, retrying: %v", err)
		}
		if !sleepContext(ctx, reconnectDelay) {
			return
		}
	}
}

func sleepContext(ctx context.Context, delay time.Duration) bool {
	timer := time.NewTimer(delay)
	defer timer.Stop()

	select {
	case <-ctx.Done():
		return false
	case <-timer.C:
		return true
	}
}

func routes(internalServiceToken string, emailSender email.Sender) http.Handler {
	mux := http.NewServeMux()
	mux.HandleFunc("/healthz", func(w http.ResponseWriter, _ *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("ok"))
	})
	mux.Handle("/internal/v1/emails/send", email.NewHandler(internalServiceToken, emailSender))
	return mux
}

func buildEmailSender(cfg config.Config) email.Sender {
	if cfg.EmailDeliveryMode == "smtp" {
		return email.NewSMTPSender(
			cfg.SMTPAddress,
			cfg.SMTPUsername,
			cfg.SMTPPassword,
			cfg.SMTPFromAddress,
			cfg.SMTPFromName,
		)
	}
	return email.NewLogSender()
}
