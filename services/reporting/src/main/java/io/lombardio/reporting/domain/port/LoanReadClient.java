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
package io.lombardio.reporting.domain.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface LoanReadClient {

  List<ReportedLoanCase> listLoans(String tenantId, String bearerToken);

  record ReportedLoanCase(
      String id,
      Instant recordedAt,
      List<ReportedLoanPosition> positions,
      List<ReportedPawnTicket> pawnTickets) {

    public ReportedLoanCase {
      positions = List.copyOf(positions != null ? positions : List.of());
      pawnTickets = List.copyOf(pawnTickets != null ? pawnTickets : List.of());
    }
  }

  record ReportedLoanPosition(String label, String guidelineLabel, BigDecimal pledgedValue) {}

  record ReportedPawnTicket(String ticketNumber, BigDecimal totalLoanValue) {}
}
