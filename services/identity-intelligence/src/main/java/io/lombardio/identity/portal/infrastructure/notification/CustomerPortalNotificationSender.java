package io.lombardio.identity.portal.infrastructure.notification;

import io.lombardio.identity.domain.model.Customer;

import java.time.Instant;

public interface CustomerPortalNotificationSender {

    void sendInvitation(Customer customer, String token, Instant expiresAt);
}
