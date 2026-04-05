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

import java.util.List;

public record LoanCaseResponse(
    String id,
    CustomerView customer,
    PledgeRecordResponse pledgeRecord,
    List<LoanPositionResponse> positions,
    List<PawnTicketResponse> pawnTickets) {

  public LoanCaseResponse {
    positions = List.copyOf(positions == null ? List.of() : positions);
    pawnTickets = List.copyOf(pawnTickets == null ? List.of() : pawnTickets);
  }

  @Override
  public List<LoanPositionResponse> positions() {
    return List.copyOf(positions);
  }

  @Override
  public List<PawnTicketResponse> pawnTickets() {
    return List.copyOf(pawnTickets);
  }
}
