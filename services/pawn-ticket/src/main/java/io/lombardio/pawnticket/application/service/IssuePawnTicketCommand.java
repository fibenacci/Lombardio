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
package io.lombardio.pawnticket.application.service;

import io.lombardio.pawnticket.domain.model.PawnTicketPosition;
import java.math.BigDecimal;
import java.util.List;

public record IssuePawnTicketCommand(
    String tenantId,
    String customerId,
    String customerNumber,
    String customerDisplayName,
    String customerPhone,
    List<PawnTicketPosition> positions,
    BigDecimal loanAmount,
    Integer termMonths,
    BigDecimal manualMonthlyOperatingFee) {

  public IssuePawnTicketCommand {
    positions = List.copyOf(positions != null ? positions : List.of());
  }
}
