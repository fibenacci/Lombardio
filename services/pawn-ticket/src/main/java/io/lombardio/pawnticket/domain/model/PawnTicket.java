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
package io.lombardio.pawnticket.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PawnTicket(
    String id,
    String tenantId,
    String customerId,
    String customerNumber,
    String customerDisplayName,
    String customerPhone,
    String contractNumber,
    String contractBarcode,
    String ticketNumber,
    String termsVersion,
    String termsAndConditionsText,
    Instant createdAt,
    LocalDate dueDate,
    LocalDate earliestAuctionDate,
    int termMonths,
    BigDecimal loanAmount,
    BigDecimal monthlyInterestRate,
    BigDecimal monthlyOperatingFee,
    boolean manualMonthlyOperatingFeeRequired,
    BigDecimal totalInterestAmount,
    BigDecimal totalOperatingFeeAmount,
    BigDecimal totalRepaymentAmount,
    String legalText,
    List<PawnTicketPosition> positions) {

  public static final BigDecimal DEFAULT_INTEREST_RATE = new BigDecimal("1.00");
  public static final String DEFAULT_LEGAL_TEXT =
      "Kostenmodell gemaess PfandlV: monatlicher Zins 1 Prozent nach § 10 Abs. 1 Nr. 1 PfandlV, Betriebsverguetung nach Anlage zu § 10 Abs. 1 Nr. 2 PfandlV bis 300 Euro Darlehensbetrag, Mindestfaelligkeit 3 Monate nach § 5 Abs. 1 PfandlV. Eine Pfandverwertung ist fruehestens einen Monat nach Faelligkeit zulaessig (§ 9 Abs. 1 PfandlV).";

  public PawnTicket {
    positions = List.copyOf(positions != null ? positions : List.of());
  }

  public static PawnTicket createQuote(
      String id,
      String termsVersion,
      String termsText,
      Instant now,
      LocalDate contractDate,
      int termMonths,
      BigDecimal loanAmount,
      BigDecimal manualMonthlyOperatingFee) {

    BigDecimal monthlyOperatingFee = resolveOperatingFee(loanAmount, manualMonthlyOperatingFee);
    boolean manualRequired = isManualOperatingFeeRequired(loanAmount, manualMonthlyOperatingFee);
    LocalDate dueDate = contractDate.plusMonths(termMonths);

    BigDecimal totalInterest = calculateTotalInterest(loanAmount, termMonths);
    BigDecimal totalOperatingFee =
        monthlyOperatingFee.multiply(new BigDecimal(termMonths)).setScale(2, RoundingMode.HALF_UP);
    BigDecimal totalRepayment =
        loanAmount.add(totalInterest).add(totalOperatingFee).setScale(2, RoundingMode.HALF_UP);

    return new PawnTicket(
        id,
        "quote",
        "quote",
        "QUOTE",
        "Angebot",
        null,
        "QUOTE",
        "QUOTE",
        "QUOTE",
        termsVersion,
        termsText,
        now,
        dueDate,
        dueDate.plusMonths(1),
        termMonths,
        loanAmount.setScale(2, RoundingMode.HALF_UP),
        DEFAULT_INTEREST_RATE,
        monthlyOperatingFee,
        manualRequired,
        totalInterest,
        totalOperatingFee,
        totalRepayment,
        DEFAULT_LEGAL_TEXT,
        List.of());
  }

  private static BigDecimal calculateTotalInterest(BigDecimal loanAmount, int termMonths) {
    return loanAmount
        .multiply(DEFAULT_INTEREST_RATE)
        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
        .multiply(new BigDecimal(termMonths))
        .setScale(2, RoundingMode.HALF_UP);
  }

  private static BigDecimal resolveOperatingFee(BigDecimal loanAmount, BigDecimal manualFee) {
    if (loanAmount.compareTo(new BigDecimal("15.00")) <= 0) return new BigDecimal("1.00");
    if (loanAmount.compareTo(new BigDecimal("30.00")) <= 0) return new BigDecimal("1.50");
    if (loanAmount.compareTo(new BigDecimal("50.00")) <= 0) return new BigDecimal("2.00");
    if (loanAmount.compareTo(new BigDecimal("100.00")) <= 0) return new BigDecimal("2.50");
    if (loanAmount.compareTo(new BigDecimal("150.00")) <= 0) return new BigDecimal("3.50");
    if (loanAmount.compareTo(new BigDecimal("200.00")) <= 0) return new BigDecimal("4.50");
    if (loanAmount.compareTo(new BigDecimal("250.00")) <= 0) return new BigDecimal("5.50");
    if (loanAmount.compareTo(new BigDecimal("300.00")) <= 0) return new BigDecimal("6.50");
    return manualFee != null
        ? manualFee.setScale(2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
  }

  private static boolean isManualOperatingFeeRequired(BigDecimal loanAmount, BigDecimal manualFee) {
    return loanAmount.compareTo(new BigDecimal("300.00")) > 0 && manualFee == null;
  }
}
