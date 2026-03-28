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
    List<TransactionMixResponse> transactionMix) {}
