package io.lombardio.pawnticket.application.service;

import io.lombardio.pawnticket.domain.model.CashTransactionType;

import java.math.BigDecimal;

public record ExecuteCashTransactionCommand(
        String tenantId,
        String ticketNumber,
        CashTransactionType type,
        BigDecimal outstandingLoanAmount,
        Integer extensionMonths,
        BigDecimal repaymentAmount,
        Integer remainingTermMonths,
        BigDecimal manualMonthlyOperatingFee,
        String note
) {
}
