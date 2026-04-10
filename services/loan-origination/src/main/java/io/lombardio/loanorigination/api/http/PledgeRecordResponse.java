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
package io.lombardio.loanorigination.api.http;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;

@Schema(requiredProperties = {
    "id", "recordedAt", "languageCode", "retentionUntil", "pledgorName", "pledgorStreet",
    "pledgorPostalCode", "pledgorCity", "pledgorBirthDate", "checkedDocumentType",
    "powerOfAttorneyRequired", "bearerName", "bearerStreet", "bearerPostalCode", "bearerCity"
})
public record PledgeRecordResponse(
    @NotNull String id,
    @NotNull Instant recordedAt,
    @NotNull String languageCode,
    @NotNull LocalDate retentionUntil,
    @NotNull String pledgorName,
    @NotNull String pledgorStreet,
    @NotNull String pledgorPostalCode,
    @NotNull String pledgorCity,
    @NotNull LocalDate pledgorBirthDate,
    @NotNull String checkedDocumentType,
    @NotNull boolean powerOfAttorneyRequired,
    @NotNull String bearerName,
    @NotNull String bearerStreet,
    @NotNull String bearerPostalCode,
    @NotNull String bearerCity) {}
