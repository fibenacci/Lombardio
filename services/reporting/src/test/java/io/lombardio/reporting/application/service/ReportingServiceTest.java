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
package io.lombardio.reporting.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lombardio.reporting.api.http.ReportingDashboardResponse;
import io.lombardio.reporting.domain.port.LoanReadClient;
import io.lombardio.reporting.domain.port.PawnTicketReadClient;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportingServiceTest {

  private final ReportingService reportingService =
      new ReportingService(
          (tenantId, bearerToken) ->
              List.of(
                  new LoanReadClient.ReportedLoanCase(
                      "loan-1",
                      Instant.parse("2026-03-15T10:15:00Z"),
                      List.of(
                          new LoanReadClient.ReportedLoanPosition(
                              "Goldring", "Goldring 585", new BigDecimal("180.00")),
                          new LoanReadClient.ReportedLoanPosition(
                              "iPhone", "Apple iPhone 14", new BigDecimal("260.00"))),
                      List.of(
                          new LoanReadClient.ReportedPawnTicket(
                              "PS-1001", new BigDecimal("180.00")),
                          new LoanReadClient.ReportedPawnTicket(
                              "PS-1002", new BigDecimal("260.00"))))),
          new PawnTicketReadClient() {
            @Override
            public List<PawnTicketReadClient.ReportedPawnTicketOverview> listTickets(
                String tenantId, String bearerToken) {
              return List.of(
                  new PawnTicketReadClient.ReportedPawnTicketOverview(
                      "PS-1001", new BigDecimal("180.00"), new BigDecimal("198.90"), 1),
                  new PawnTicketReadClient.ReportedPawnTicketOverview(
                      "PS-1002", new BigDecimal("260.00"), new BigDecimal("281.30"), 1));
            }

            @Override
            public List<PawnTicketReadClient.ReportedCashTransaction> listCashTransactions(
                String tenantId, String bearerToken) {
              return List.of(
                  new PawnTicketReadClient.ReportedCashTransaction(
                      "REDEEM",
                      new BigDecimal("6.00"),
                      new BigDecimal("13.50"),
                      new BigDecimal("219.50"),
                      Instant.parse("2026-03-16T12:00:00Z")),
                  new PawnTicketReadClient.ReportedCashTransaction(
                      "EXTEND",
                      new BigDecimal("7.80"),
                      new BigDecimal("13.50"),
                      new BigDecimal("281.30"),
                      Instant.parse("2026-03-17T08:30:00Z")));
            }
          },
          Clock.fixed(Instant.parse("2026-03-18T12:00:00Z"), ZoneOffset.UTC));

  @Test
  void shouldAggregateDashboardFinanceAndInventory() {
    ReportingDashboardResponse dashboard =
        reportingService.getDashboard("tenant-default", 14, "token-123");

    assertEquals(new BigDecimal("500.80"), dashboard.finance().cashInflow());
    assertEquals(new BigDecimal("440.00"), dashboard.finance().cashOutflow());
    assertEquals(new BigDecimal("60.80"), dashboard.finance().netCashflow());
    assertEquals(new BigDecimal("40.80"), dashboard.finance().realizedRevenue());
    assertEquals(2, dashboard.finance().activeTicketCount());
    assertEquals("Apple iPhone 14", dashboard.inventoryByCategory().get(0).category());
    assertEquals("EXTEND", dashboard.transactionMix().get(0).type());
  }
}
