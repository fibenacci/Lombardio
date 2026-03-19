package io.lombardio.pawnticket.application.service;

import io.lombardio.pawnticket.domain.model.PawnTicketPosition;

import java.math.BigDecimal;
import java.util.List;

public record IssuePawnTicketCommand(
        String tenantId,
        String customerId,
        String customerNumber,
        String customerDisplayName,
        String customerPhone,
        List<PawnTicketPosition> positions,
        BigDecimal loanAmount,
        Integer termMonths,
        BigDecimal manualMonthlyOperatingFee
) {
}
