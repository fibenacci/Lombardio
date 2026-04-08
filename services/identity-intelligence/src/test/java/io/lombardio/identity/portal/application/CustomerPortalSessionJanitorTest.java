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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CustomerPortalSessionJanitorTest {

  @Test
  void deletesExpiredSessionsUsingCurrentClockInstant() {
    CustomerPortalSessionStore sessionStore = mock(CustomerPortalSessionStore.class);
    Clock clock = Clock.fixed(Instant.parse("2026-04-02T00:00:00Z"), ZoneOffset.UTC);
    CustomerPortalSessionJanitor janitor = new CustomerPortalSessionJanitor(sessionStore, clock);

    janitor.deleteExpiredSessions();

    verify(sessionStore).deleteExpiredBefore(Instant.parse("2026-04-02T00:00:00Z"));
  }
}
