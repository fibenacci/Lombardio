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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.pawnticket.domain.model.CashTransactionType;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.util.Locale;

public class PawnTicketMetrics {

  private final MeterRegistry meterRegistry;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification =
          "MeterRegistry is a managed metrics dependency that must be retained directly")
  public PawnTicketMetrics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public static PawnTicketMetrics noop() {
    return new PawnTicketMetrics(new SimpleMeterRegistry());
  }

  public void recordIssued(BigDecimal loanAmount, int positionCount) {
    meterRegistry.counter("lombardio.pawn_ticket.issued").increment();
    DistributionSummary.builder("lombardio.pawn_ticket.loan_amount")
        .baseUnit("eur")
        .register(meterRegistry)
        .record(loanAmount.doubleValue());
    DistributionSummary.builder("lombardio.pawn_ticket.position_count")
        .register(meterRegistry)
        .record(positionCount);
  }

  public void recordCashTransaction(CashTransactionType type, BigDecimal totalAmount) {
    String transactionType = type.name().toLowerCase(Locale.ROOT);
    meterRegistry
        .counter("lombardio.cash_transaction.executed", "type", transactionType)
        .increment();
    DistributionSummary.builder("lombardio.cash_transaction.total_amount")
        .baseUnit("eur")
        .tag("type", transactionType)
        .register(meterRegistry)
        .record(totalAmount.doubleValue());
  }
}
