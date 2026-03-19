package io.lombardio.reporting.api.http;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ReportingDashboardResponse(
        LocalDate rangeStart,
        LocalDate rangeEnd,
        Instant generatedAt,
        FinanceSummaryResponse finance,
        List<FinanceTrendPointResponse> financeTrend,
        List<InventoryCategoryResponse> inventoryByCategory,
        List<TransactionMixResponse> transactionMix
) {
}
