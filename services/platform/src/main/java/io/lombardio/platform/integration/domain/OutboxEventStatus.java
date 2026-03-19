package io.lombardio.platform.integration.domain;

public enum OutboxEventStatus {
    PENDING,
    PROCESSING,
    PUBLISHED
}
