package io.lombardio.loanorigination.api.http;

import java.math.BigDecimal;

public record ValuationGuidelineResponse(
        String id,
        String category,
        String material,
        String label,
        String description,
        BigDecimal baseLoanValue
) {
}
