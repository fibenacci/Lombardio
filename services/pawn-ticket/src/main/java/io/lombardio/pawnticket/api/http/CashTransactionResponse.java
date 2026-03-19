package io.lombardio.pawnticket.api.http;

import io.lombardio.pawnticket.domain.model.CashTransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record CashTransactionResponse(
        String id,
        String ticketNumber,
        String customerNumber,
        String customerDisplayName,
        CashTransactionType type,
        BigDecimal outstandingLoanAmount,
        BigDecimal interestAmount,
        BigDecimal operatingFeeAmount,
        BigDecimal totalAmount,
        String legalText,
        String note,
        Instant createdAt
) {
}
