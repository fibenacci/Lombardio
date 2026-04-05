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
    List<TransactionMixResponse> transactionMix) {

  public ReportingDashboardResponse {
    financeTrend = List.copyOf(financeTrend == null ? List.of() : financeTrend);
    inventoryByCategory =
        List.copyOf(inventoryByCategory == null ? List.of() : inventoryByCategory);
    transactionMix = List.copyOf(transactionMix == null ? List.of() : transactionMix);
  }

  @Override
  public List<FinanceTrendPointResponse> financeTrend() {
    return List.copyOf(financeTrend);
  }

  @Override
  public List<InventoryCategoryResponse> inventoryByCategory() {
    return List.copyOf(inventoryByCategory);
  }

  @Override
  public List<TransactionMixResponse> transactionMix() {
    return List.copyOf(transactionMix);
  }
}
