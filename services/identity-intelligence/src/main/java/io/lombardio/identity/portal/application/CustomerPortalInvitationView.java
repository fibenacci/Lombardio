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
package io.lombardio.identity.portal.application;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CustomerPortalInvitationView(
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String customerDisplayName,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String email,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String status) {}
