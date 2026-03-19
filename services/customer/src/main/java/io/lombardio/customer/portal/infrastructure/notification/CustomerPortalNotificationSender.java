package io.lombardio.customer.portal.infrastructure.notification;

import io.lombardio.customer.domain.model.Customer;

import java.time.Instant;

public interface CustomerPortalNotificationSender {

    void sendInvitation(Customer customer, String token, Instant expiresAt);
}
