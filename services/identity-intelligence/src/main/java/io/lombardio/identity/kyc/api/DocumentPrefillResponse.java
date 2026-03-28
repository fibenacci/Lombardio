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

import java.time.LocalDate;

public record DocumentPrefillResponse(
    boolean available,
    boolean matched,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String documentType,
    String documentNumber,
    LocalDate documentValidUntil,
    String portraitImageDataUrl,
    String providerName,
    double confidence) {}
