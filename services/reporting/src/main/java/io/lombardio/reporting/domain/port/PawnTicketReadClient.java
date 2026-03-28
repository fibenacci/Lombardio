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

public interface PawnTicketReadClient {

  List<ReportedPawnTicketOverview> listTickets(String tenantId, String bearerToken);

  List<ReportedCashTransaction> listCashTransactions(String tenantId, String bearerToken);

  record ReportedPawnTicketOverview(
      String ticketNumber,
      BigDecimal totalLoanValue,
      BigDecimal totalRepaymentAmount,
      Integer positionCount) {}

  record ReportedCashTransaction(
      String type,
      BigDecimal interestAmount,
      BigDecimal operatingFeeAmount,
      BigDecimal totalAmount,
      Instant createdAt) {}
}
