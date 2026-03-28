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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.util.Locale;

public class LoanOriginationMetrics {

  private final MeterRegistry meterRegistry;
  private final Counter createdCounter;
  private final DistributionSummary loanAmountSummary;

  public LoanOriginationMetrics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    this.createdCounter =
        Counter.builder("lombardio.loan_origination.created")
            .description("Successfully created loan originations")
            .register(meterRegistry);
    this.loanAmountSummary =
        DistributionSummary.builder("lombardio.loan_origination.loan_amount")
            .baseUnit("eur")
            .description("Total loan amount of created originations")
            .register(meterRegistry);
  }

  public static LoanOriginationMetrics noop() {
    return new LoanOriginationMetrics(new SimpleMeterRegistry());
  }

  public void recordCreated(BigDecimal totalLoanAmount) {
    createdCounter.increment();
    loanAmountSummary.record(totalLoanAmount.doubleValue());
  }

  public void recordRejected(String reason) {
    meterRegistry
        .counter("lombardio.loan_origination.rejected", "reason", normalizeReason(reason))
        .increment();
  }

  private String normalizeReason(String reason) {
    if (reason == null || reason.isBlank()) {
      return "unknown";
    }
    String normalized = reason.toLowerCase(Locale.ROOT);
    if (normalized.contains("kyc")) {
      return "kyc";
    }
    if (normalized.contains("aml")) {
      return "aml";
    }
    if (normalized.contains("powerofattorney") || normalized.contains("power of attorney")) {
      return "power_of_attorney";
    }
    if (normalized.contains("valuation guideline")) {
      return "valuation_guideline";
    }
    return "validation";
  }
}
