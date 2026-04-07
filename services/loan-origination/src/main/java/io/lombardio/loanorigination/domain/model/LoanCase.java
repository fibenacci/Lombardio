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
package io.lombardio.loanorigination.domain.model;

import java.util.List;
import java.util.UUID;

public record LoanCase(
    String id,
    String tenantId,
    CustomerProfile customer,
    PledgeRecord pledgeRecord,
    List<LoanPosition> positions,
    List<PawnTicket> pawnTickets) {

  public LoanCase {
    positions = List.copyOf(positions != null ? positions : List.of());
    pawnTickets = List.copyOf(pawnTickets != null ? pawnTickets : List.of());
  }

  public static LoanCase create(
      String tenantId,
      CustomerProfile customer,
      PledgeRecord pledgeRecord,
      List<LoanPosition> positions,
      List<PawnTicket> pawnTickets) {
    return new LoanCase(
        "loan-" + UUID.randomUUID(), tenantId, customer, pledgeRecord, positions, pawnTickets);
  }
}
