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
package io.lombardio.pawnticket.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.lombardio.pawnticket.infrastructure.persistence.support.InMemoryRepositories;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class PawnTicketPolicyServiceTest {

  private final PawnTicketPolicyService service =
      new PawnTicketPolicyService(
          new InMemoryRepositories.PawnTickets(),
          new PawnTicketTermsService(),
          Clock.fixed(Instant.parse("2026-03-18T12:00:00Z"), ZoneOffset.UTC));

  @Test
  void shouldUseStatutoryOperatingFeeUpToThreeHundredEuro() {
    var quote = service.quote(new PawnTicketQuoteCommand(new BigDecimal("200.00"), 3, null));

    assertEquals(new BigDecimal("1.00"), quote.monthlyInterestRate());
    assertEquals(new BigDecimal("4.50"), quote.monthlyOperatingFee());
    assertEquals(new BigDecimal("6.00"), quote.totalInterestAmount());
    assertEquals(new BigDecimal("13.50"), quote.totalOperatingFeeAmount());
    assertEquals(new BigDecimal("219.50"), quote.totalRepaymentAmount());
    assertEquals("2026-06-18", quote.dueDate().toString());
    assertEquals("2026-07-18", quote.earliestAuctionDate().toString());
  }

  @Test
  void shouldRequireManualOperatingFeeAboveThreeHundredEuro() {
    var quote = service.quote(new PawnTicketQuoteCommand(new BigDecimal("500.00"), 3, null));

    assertTrue(quote.manualMonthlyOperatingFeeRequired());
    assertEquals(new BigDecimal("0.00"), quote.monthlyOperatingFee());
  }

  @Test
  void shouldCalculatePartialRepaymentSettlement() {
    var settlement =
        service.settlePartial(
            new PawnTicketSettlementCommand(
                new BigDecimal("200.00"), new BigDecimal("50.00"), 3, null, null));

    assertEquals(new BigDecimal("150.00"), settlement.outstandingLoanAmount());
    assertEquals(new BigDecimal("4.50"), settlement.interestAmount());
    assertEquals(new BigDecimal("10.50"), settlement.operatingFeeAmount());
    assertEquals(new BigDecimal("165.00"), settlement.totalDueAmount());
  }

  @Test
  void shouldCalculateRedemptionAmount() {
    var settlement =
        service.redeem(
            new PawnTicketSettlementCommand(new BigDecimal("200.00"), null, 3, null, null));

    assertEquals(new BigDecimal("200.00"), settlement.outstandingLoanAmount());
    assertEquals(new BigDecimal("6.00"), settlement.interestAmount());
    assertEquals(new BigDecimal("13.50"), settlement.operatingFeeAmount());
    assertEquals(new BigDecimal("219.50"), settlement.totalDueAmount());
  }

  @Test
  void shouldIssueAndLoadStructuredPawnTicket() {
    var issued =
        service.issue(
            new IssuePawnTicketCommand(
                "tenant-default",
                "customer-1",
                "KD-1001",
                "Anna Becker",
                "+49 170 111111",
                List.of(
                    new io.lombardio.pawnticket.domain.model.PawnTicketPosition(
                        null, null, "Goldring 585", "Gelbgold 14 Karat", new BigDecimal("180.00"))),
                new BigDecimal("180.00"),
                3,
                null));

    var loaded = service.getIssuedTicket(issued.ticketNumber());

    assertEquals("tenant-default", loaded.tenantId());
    assertEquals("Anna Becker", loaded.customerDisplayName());
    assertEquals(1, loaded.positions().size());
    assertNotNull(loaded.ticketNumber());
    assertEquals(loaded.contractNumber() + "-01", loaded.positions().get(0).itemNumber());
  }

  @Test
  void shouldListIssuedTicketsForTenant() {
    service.issue(
        new IssuePawnTicketCommand(
            "tenant-default",
            "customer-1",
            "KD-1001",
            "Anna Becker",
            "+49 170 111111",
            List.of(
                new io.lombardio.pawnticket.domain.model.PawnTicketPosition(
                    null, null, "Goldring 585", "Gelbgold 14 Karat", new BigDecimal("180.00"))),
            new BigDecimal("180.00"),
            3,
            null));
    service.issue(
        new IssuePawnTicketCommand(
            "tenant-other",
            "customer-2",
            "KD-2001",
            "Murat Yilmaz",
            "+49 170 222222",
            List.of(
                new io.lombardio.pawnticket.domain.model.PawnTicketPosition(
                    null, null, "Apple iPhone 14", "128GB", new BigDecimal("260.00"))),
            new BigDecimal("260.00"),
            3,
            null));

    var tenantTickets = service.listIssuedTickets("tenant-default");

    assertEquals(1, tenantTickets.size());
    assertEquals("tenant-default", tenantTickets.get(0).tenantId());
    assertEquals("Anna Becker", tenantTickets.get(0).customerDisplayName());
  }
}
