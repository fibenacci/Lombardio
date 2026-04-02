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
package io.lombardio.platform.config;

import java.util.Map;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.operator-bff")
public record OperatorBffProperties(
    String identityBaseUrl,
    String originationBaseUrl,
    String pawnTicketBaseUrl,
    String auctionBaseUrl,
    String onlineAuctionBaseUrl,
    String reportingBaseUrl) {

  public OperatorBffProperties {
    requireNonBlank(identityBaseUrl, "identityBaseUrl");
    requireNonBlank(originationBaseUrl, "originationBaseUrl");
    requireNonBlank(pawnTicketBaseUrl, "pawnTicketBaseUrl");
    requireNonBlank(auctionBaseUrl, "auctionBaseUrl");
    requireNonBlank(onlineAuctionBaseUrl, "onlineAuctionBaseUrl");
    requireNonBlank(reportingBaseUrl, "reportingBaseUrl");
  }

  public Optional<String> resolve(String serviceKey) {
    return Optional.ofNullable(
        switch (serviceKey) {
          case "identity" -> identityBaseUrl;
          case "origination" -> originationBaseUrl;
          case "pawn-ticket" -> pawnTicketBaseUrl;
          case "auction" -> auctionBaseUrl;
          case "online-auction" -> onlineAuctionBaseUrl;
          case "reporting" -> reportingBaseUrl;
          default -> null;
        });
  }

  public Map<String, String> targets() {
    return Map.of(
        "identity", identityBaseUrl,
        "origination", originationBaseUrl,
        "pawn-ticket", pawnTicketBaseUrl,
        "auction", auctionBaseUrl,
        "online-auction", onlineAuctionBaseUrl,
        "reporting", reportingBaseUrl);
  }

  private static void requireNonBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
  }
}
