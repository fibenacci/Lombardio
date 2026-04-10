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
package io.lombardio.onlineauction.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(requiredProperties = {"wsUrl", "channel", "connectionToken", "subscriptionToken"})
public record RealtimeSessionResponse(
    @NotNull String wsUrl,
    @NotNull String channel,
    @NotNull String connectionToken,
    @NotNull String subscriptionToken) {}
