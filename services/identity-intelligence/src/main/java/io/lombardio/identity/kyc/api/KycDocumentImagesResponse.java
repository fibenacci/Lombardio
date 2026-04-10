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
package io.lombardio.identity.kyc.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record KycDocumentImagesResponse(
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String customerId,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String documentFrontImageDataUrl,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String documentBackImageDataUrl) {}
