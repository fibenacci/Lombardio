/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.platform.integration.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.rabbitmq")
@SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "Configuration properties")
public record IntegrationRabbitMqProperties(
    String exchange, int publisherBatchSize, boolean publisherEnabled) {}
