package io.lombardio.pawnticket.api.http;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PawnTicketResponse(
        String contractNumber,
        String contractBarcode,
        String ticketNumber,
        String termsVersion,
        String termsAndConditionsText,
        Instant createdAt,
        LocalDate dueDate,
        LocalDate earliestAuctionDate,
        Integer termMonths,
        BigDecimal totalLoanValue,
        BigDecimal monthlyInterestRate,
        BigDecimal monthlyOperatingFee,
        boolean manualMonthlyOperatingFeeRequired,
        BigDecimal totalInterestAmount,
        BigDecimal totalOperatingFeeAmount,
        BigDecimal totalRepaymentAmount,
        String legalText,
        java.util.List<PawnTicketPositionResponse> positions
) {
}
