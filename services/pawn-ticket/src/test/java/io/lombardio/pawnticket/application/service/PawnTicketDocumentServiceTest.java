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

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.lombardio.pawnticket.domain.model.PawnTicket;
import io.lombardio.pawnticket.domain.model.PawnTicketPosition;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PawnTicketDocumentServiceTest {

  private final PawnTicketDocumentService service = new PawnTicketDocumentService();

  @Test
  void shouldRenderStructuredPdfDocument() {
    PawnTicket pawnTicket =
        new PawnTicket(
            "ticket-1",
            "tenant-default",
            "customer-1",
            "KD-1001",
            "Anna Becker",
            "+49 170 111111",
            "PS-5001",
            "PS-5001",
            "PS-5001",
            "AGB-2026-03",
            "AGB Text",
            Instant.parse("2026-03-18T12:00:00Z"),
            LocalDate.parse("2026-06-18"),
            LocalDate.parse("2026-07-18"),
            3,
            new BigDecimal("180.00"),
            new BigDecimal("1.00"),
            new BigDecimal("4.50"),
            false,
            new BigDecimal("5.40"),
            new BigDecimal("13.50"),
            new BigDecimal("198.90"),
            "Rechtshinweis",
            List.of(
                new PawnTicketPosition(
                    "PS-5001-01",
                    "PS-5001-01",
                    "Goldring 585",
                    "Gelbgold 14 Karat",
                    new BigDecimal("180.00"))));

    byte[] pdf = service.render(pawnTicket);

    assertTrue(pdf.length > 500);
    assertTrue(new String(pdf, 0, 4, StandardCharsets.US_ASCII).startsWith("%PDF"));
  }
}
