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

import io.lombardio.pawnticket.domain.model.CashTransactionType;
import io.lombardio.pawnticket.domain.model.PawnTicketPosition;
import io.lombardio.pawnticket.infrastructure.persistence.support.InMemoryRepositories;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class CashTransactionServiceTest {

  private final InMemoryRepositories.PawnTickets pawnTicketRepository =
      new InMemoryRepositories.PawnTickets();
  private final PawnTicketPolicyService pawnTicketPolicyService =
      new PawnTicketPolicyService(
          pawnTicketRepository,
          new PawnTicketTermsService(),
          Clock.fixed(Instant.parse("2026-03-18T12:00:00Z"), ZoneOffset.UTC));
  private final CashTransactionService cashTransactionService =
      new CashTransactionService(
          pawnTicketRepository,
          new InMemoryRepositories.CashTransactions(),
          pawnTicketPolicyService,
          Clock.fixed(Instant.parse("2026-03-18T12:00:00Z"), ZoneOffset.UTC));

  @Test
  void shouldPersistRedeemTransactionInJournal() {
    pawnTicketPolicyService.issue(
        new IssuePawnTicketCommand(
            "tenant-default",
            "customer-1",
            "KD-1001",
            "Anna Becker",
            "+49 170 111111",
            List.of(
                new PawnTicketPosition(
                    null, null, "Goldring 585", "Gelbgold 14 Karat", new BigDecimal("180.00"))),
            new BigDecimal("180.00"),
            3,
            null));

    var transaction =
        cashTransactionService.execute(
            new ExecuteCashTransactionCommand(
                "tenant-default",
                "PS-5001",
                CashTransactionType.REDEEM,
                new BigDecimal("180.00"),
                null,
                null,
                3,
                null,
                "Bar ausgezahlt"));

    assertEquals("PS-5001", transaction.ticketNumber());
    assertEquals(CashTransactionType.REDEEM, transaction.type());
    assertEquals(new BigDecimal("198.90"), transaction.totalAmount());
    assertEquals(1, cashTransactionService.listTransactions("tenant-default").size());
  }
}
