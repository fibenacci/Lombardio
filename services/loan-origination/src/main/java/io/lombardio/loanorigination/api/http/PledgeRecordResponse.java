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

import java.time.Instant;
import java.time.LocalDate;

public record PledgeRecordResponse(
    String id,
    Instant recordedAt,
    String languageCode,
    LocalDate retentionUntil,
    String pledgorName,
    String pledgorStreet,
    String pledgorPostalCode,
    String pledgorCity,
    LocalDate pledgorBirthDate,
    String checkedDocumentType,
    boolean powerOfAttorneyRequired,
    String bearerName,
    String bearerStreet,
    String bearerPostalCode,
    String bearerCity) {}
