package io.lombardio.reporting.api.http;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinanceTrendPointResponse(
        LocalDate date,
        BigDecimal cashInflow,
        BigDecimal cashOutflow,
        BigDecimal realizedRevenue
) {
}
