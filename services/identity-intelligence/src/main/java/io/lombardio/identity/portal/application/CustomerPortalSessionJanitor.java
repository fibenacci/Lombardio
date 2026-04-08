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
package io.lombardio.identity.portal.application;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Clock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CustomerPortalSessionJanitor {

  private final CustomerPortalSessionStore sessionStore;
  private final Clock clock;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "Spring-managed repository dependency cannot be defensively copied")
  public CustomerPortalSessionJanitor(CustomerPortalSessionStore sessionStore, Clock clock) {
    this.sessionStore = sessionStore;
    this.clock = clock;
  }

  @Scheduled(fixedDelayString = "${customer-portal.session.cleanup-fixed-delay-ms}")
  public void deleteExpiredSessions() {
    sessionStore.deleteExpiredBefore(clock.instant());
  }
}
