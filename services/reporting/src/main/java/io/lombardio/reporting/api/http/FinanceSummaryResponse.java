package io.lombardio.reporting.api.http;

import java.math.BigDecimal;

public record FinanceSummaryResponse(
        BigDecimal cashInflow,
        BigDecimal cashOutflow,
        BigDecimal netCashflow,
        BigDecimal realizedRevenue,
        BigDecimal activeLoanExposure,
        Integer activeTicketCount,
        BigDecimal averageTicketValue
) {
}
