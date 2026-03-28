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
package io.lombardio.loanorigination.application.service;

import java.math.BigDecimal;
import java.util.List;

public record CreateLoanCommand(
    String customerId,
    List<CreateLoanPositionCommand> positions,
    Integer termMonths,
    BigDecimal manualMonthlyOperatingFee,
    boolean thirdPartyPledgorPresentation,
    String bearerName,
    String bearerStreet,
    String bearerPostalCode,
    String bearerCity,
    String powerOfAttorneyDocumentDataUrl) {}
