package io.lombardio.loanorigination.domain.model;

import java.math.BigDecimal;

public record ValuationGuideline(
        String id,
        String tenantId,
        String category,
        String material,
        String label,
        String description,
        BigDecimal baseLoanValue
) {
}
