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
package io.lombardio.identity.kyc.application;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(
    requiredProperties = {
      "available",
      "matched",
      "firstName",
      "lastName",
      "birthDate",
      "documentType",
      "documentNumber",
      "documentValidUntil",
      "portraitImageDataUrl",
      "providerName",
      "confidence"
    })
public record DocumentPrefillView(
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean available,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean matched,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String firstName,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String lastName,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate birthDate,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String documentType,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String documentNumber,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate documentValidUntil,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String portraitImageDataUrl,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String providerName,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) double confidence) {}
