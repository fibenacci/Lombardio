package io.lombardio.customer.portal.infrastructure.ticket;

import io.lombardio.customer.portal.api.CustomerPortalPawnTicketResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;

@Component
public class HttpCustomerPortalTicketClient implements CustomerPortalTicketClient {

    private static final String INTERNAL_AUTH_HEADER = "X-Internal-Service-Token";

    private final RestClient restClient;
    private final String internalServiceToken;

    public HttpCustomerPortalTicketClient(
            RestClient.Builder restClientBuilder,
            @Value("${pawn-ticket.base-url:http://localhost:8085}") String pawnTicketBaseUrl,
            @Value("${customer.internal-service-token:dev-internal-token}") String internalServiceToken
    ) {
        this.restClient = restClientBuilder.baseUrl(pawnTicketBaseUrl).build();
        this.internalServiceToken = internalServiceToken;
    }

    @Override
    public List<CustomerPortalPawnTicketResponse> listTickets(String tenantId, String customerId) {
        try {
            CustomerPortalPawnTicketResponse[] response = restClient.get()
                    .uri("/api/internal/v1/customers/{tenantId}/{customerId}/pawn-tickets", tenantId, customerId)
                    .header(INTERNAL_AUTH_HEADER, internalServiceToken)
                    .retrieve()
                    .body(CustomerPortalPawnTicketResponse[].class);
            return response == null ? List.of() : Arrays.asList(response);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Pawn ticket service unavailable", exception);
        }
    }

    @Override
    public byte[] downloadDocument(String tenantId, String customerId, String ticketNumber) {
        try {
            return restClient.get()
                    .uri("/api/internal/v1/customers/{tenantId}/{customerId}/pawn-tickets/{ticketNumber}/document", tenantId, customerId, ticketNumber)
                    .header(INTERNAL_AUTH_HEADER, internalServiceToken)
                    .header(HttpHeaders.ACCEPT, "application/pdf")
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Pawn ticket service unavailable", exception);
        }
    }
}
