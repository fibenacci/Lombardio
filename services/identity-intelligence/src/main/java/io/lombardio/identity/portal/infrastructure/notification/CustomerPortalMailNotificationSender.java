package io.lombardio.identity.portal.infrastructure.notification;

import io.lombardio.identity.domain.model.Customer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class CustomerPortalMailNotificationSender implements CustomerPortalNotificationSender {

    private final String portalBaseUrl;
    private final IntegrationMailClient integrationMailClient;

    public CustomerPortalMailNotificationSender(
            @Value("${customer-portal.public-base-url:http://localhost:5173/portal}") String portalBaseUrl,
            IntegrationMailClient integrationMailClient
    ) {
        this.portalBaseUrl = portalBaseUrl;
        this.integrationMailClient = integrationMailClient;
    }

    @Override
    public void sendInvitation(Customer customer, String token, Instant expiresAt) {
        String activationLink = portalBaseUrl + "/activate/" + token;
        String expiresAtText = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC).format(expiresAt);
        integrationMailClient.send(
                customer.tenantId(),
                List.of(customer.email()),
                "Ihr Zugang zum digitalen Pfandschein",
                """
                Guten Tag %s,

                Ihr Zugang zum digitalen Pfandschein wurde vorbereitet.
                Bitte aktivieren Sie Ihren Zugang unter folgendem Link:
                %s

                Der Link ist gueltig bis %s.
                """.formatted(customer.displayName(), activationLink, expiresAtText),
                """
                <p>Guten Tag %s,</p>
                <p>Ihr Zugang zum digitalen Pfandschein wurde vorbereitet.</p>
                <p><a href="%s">Zugang aktivieren</a></p>
                <p>Der Link ist gueltig bis %s.</p>
                """.formatted(escapeHtml(customer.displayName()), activationLink, expiresAtText),
                Map.of(
                        "category", "customer-portal-invitation",
                        "customerId", customer.id()
                )
        );
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
