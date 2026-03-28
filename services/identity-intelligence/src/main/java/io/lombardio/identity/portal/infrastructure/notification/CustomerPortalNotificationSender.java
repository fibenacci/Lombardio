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
import java.time.Instant;

public interface CustomerPortalNotificationSender {

  void sendInvitation(Customer customer, String token, Instant expiresAt);
}
