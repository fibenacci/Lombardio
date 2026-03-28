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

import org.springframework.stereotype.Service;

@Service
public class PawnTicketTermsService {

  private static final String TERMS_VERSION = "AGB-2026-03";
  private static final String TERMS_TEXT =
      """
    Geschaeftsbedingungen fuer den Pfandleihvertrag:
    1. Der Pfandschein ist Vertragsnachweis und bei Ausloesung vorzulegen.
    2. Das Pfand wird nur gegen vollstaendige Zahlung von Darlehen, Zinsen und Gebuehren herausgegeben.
    3. Nach Ablauf der gesetzlichen Fristen kann das Pfand verwertet werden.
    4. Änderungen und Nebenabreden beduerfen der dokumentierten Vereinbarung im System und auf dem Pfandschein.
    5. Es gelten im Uebrigen die gesetzlichen Bestimmungen der Pfandleiherverordnung und des BGB.
    """;

  public TermsSnapshot currentTerms() {
    return new TermsSnapshot(TERMS_VERSION, TERMS_TEXT);
  }

  public record TermsSnapshot(String version, String text) {}
}
