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

import java.time.LocalDate;

public record CustomerView(
    String id,
    String customerNumber,
    String displayName,
    LocalDate birthDate,
    String phone,
    String checkedDocumentType) {}
