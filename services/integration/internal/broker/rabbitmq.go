package broker

import (
	"context"
	"encoding/json"
	"fmt"

	amqp "github.com/rabbitmq/amqp091-go"

	"io.lombardio/integration-service/internal/events"
)

type Consumer struct {
	connection *amqp.Connection
	channel    *amqp.Channel
	queueName  string
}

func NewConsumer(url string, exchange string, queueName string, bindingKey string) (*Consumer, error) {
	connection, err := amqp.Dial(url)
	if err != nil {
		return nil, fmt.Errorf("connect rabbitmq: %w", err)
	}

	channel, err := connection.Channel()
	if err != nil {
		_ = connection.Close()
		return nil, fmt.Errorf("open rabbitmq channel: %w", err)
	}

	if err := channel.ExchangeDeclare(exchange, "topic", true, false, false, false, nil); err != nil {
		_ = channel.Close()
		_ = connection.Close()
		return nil, fmt.Errorf("declare exchange: %w", err)
	}

	queue, err := channel.QueueDeclare(queueName, true, false, false, false, nil)
	if err != nil {
		_ = channel.Close()
		_ = connection.Close()
		return nil, fmt.Errorf("declare queue: %w", err)
	}

	if err := channel.QueueBind(queue.Name, bindingKey, exchange, false, nil); err != nil {
		_ = channel.Close()
		_ = connection.Close()
		return nil, fmt.Errorf("bind queue: %w", err)
	}

	return &Consumer{
		connection: connection,
		channel:    channel,
		queueName:  queue.Name,
	}, nil
}

func (c *Consumer) Consume(ctx context.Context, handler func(context.Context, events.Event) error) error {
	deliveries, err := c.channel.Consume(c.queueName, "", false, false, false, false, nil)
	if err != nil {
		return fmt.Errorf("consume queue: %w", err)
	}

	for {
		select {
		case <-ctx.Done():
			return nil
		case delivery, ok := <-deliveries:
			if !ok {
				return fmt.Errorf("rabbitmq deliveries channel closed")
			}

			var event events.Event
			if err := json.Unmarshal(delivery.Body, &event); err != nil {
				_ = delivery.Nack(false, false)
				continue
			}

			if err := handler(ctx, event); err != nil {
				_ = delivery.Nack(false, true)
				continue
			}

			_ = delivery.Ack(false)
		}
	}
}

func (c *Consumer) Close() error {
	if c.channel != nil {
		_ = c.channel.Close()
	}
	if c.connection != nil {
		return c.connection.Close()
	}
	return nil
}
