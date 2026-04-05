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
package io.lombardio.identity.portal.infrastructure.notification;

import io.lombardio.identity.domain.model.Customer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CustomerPortalMailNotificationSender implements CustomerPortalNotificationSender {

  private final String portalBaseUrl;
  private final IntegrationMailClient integrationMailClient;

  public CustomerPortalMailNotificationSender(
      @Value("${customer-portal.public-base-url:http://localhost:5173/portal}")
          String portalBaseUrl,
      IntegrationMailClient integrationMailClient) {
    this.portalBaseUrl = portalBaseUrl;
    this.integrationMailClient = integrationMailClient;
  }

  @Override
  public void sendInvitation(Customer customer, String token, Instant expiresAt) {
    String activationLink =
        portalBaseUrl + "/activate#" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    String expiresAtText =
        DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC).format(expiresAt);
    String textBody =
        String.format(
            "Guten Tag %s,%n%n"
                + "Ihr Zugang zum digitalen Pfandschein wurde vorbereitet.%n"
                + "Bitte aktivieren Sie Ihren Zugang unter folgendem Link:%n"
                + "%s%n%n"
                + "Der Link ist gueltig bis %s.%n",
            customer.displayName(), activationLink, expiresAtText);
    String htmlBody =
        String.format(
            "<p>Guten Tag %s,</p>%n"
                + "<p>Ihr Zugang zum digitalen Pfandschein wurde vorbereitet.</p>%n"
                + "<p><a href=\"%s\">Zugang aktivieren</a></p>%n"
                + "<p>Der Link ist gueltig bis %s.</p>%n",
            escapeHtml(customer.displayName()), activationLink, expiresAtText);
    integrationMailClient.send(
        customer.tenantId(),
        List.of(customer.email()),
        "Ihr Zugang zum digitalen Pfandschein",
        textBody,
        htmlBody,
        Map.of("category", "customer-portal-invitation", "customerId", customer.id()));
  }

  private String escapeHtml(String value) {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
