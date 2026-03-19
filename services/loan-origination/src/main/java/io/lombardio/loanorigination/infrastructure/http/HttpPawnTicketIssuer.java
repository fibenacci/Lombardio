package io.lombardio.loanorigination.infrastructure.http;

import io.lombardio.loanorigination.domain.model.CustomerProfile;
import io.lombardio.loanorigination.domain.model.LoanPosition;
import io.lombardio.loanorigination.domain.model.PawnTicket;
import io.lombardio.loanorigination.domain.model.PawnTicketPosition;
import io.lombardio.loanorigination.domain.port.PawnTicketIssuer;
import io.lombardio.loanorigination.infrastructure.security.RequestAuthorizationTokenResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;

@Component
public class HttpPawnTicketIssuer implements PawnTicketIssuer {

    private final RestClient restClient;
    private final RequestAuthorizationTokenResolver tokenResolver;

    public HttpPawnTicketIssuer(
            RestClient.Builder restClientBuilder,
            @Value("${pawn-ticket.base-url:http://localhost:8085}") String pawnTicketBaseUrl,
            RequestAuthorizationTokenResolver tokenResolver
    ) {
        this.restClient = restClientBuilder.baseUrl(pawnTicketBaseUrl).build();
        this.tokenResolver = tokenResolver;
    }

    @Override
    public PawnTicket issue(
            String tenantId,
            CustomerProfile customer,
            List<LoanPosition> positions,
            BigDecimal loanAmount,
            Integer termMonths,
            BigDecimal manualMonthlyOperatingFee
        ) {
        try {
            RestClient.RequestBodySpec request = restClient.post().uri("/api/v1/pawn-tickets/issue");
            tokenResolver.resolveBearerToken()
                    .ifPresent(token -> request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
            PawnTicketRecord response = request
                    .body(new IssueRequest(
                            tenantId,
                            customer.id(),
                            customer.customerNumber(),
                            customer.displayName(),
                            customer.phone(),
                            loanAmount,
                            termMonths,
                            manualMonthlyOperatingFee,
                            positions.stream()
                                    .map(position -> new PositionPayload(position.label(), position.description(), position.pledgedValue()))
                                    .toList()
                    ))
                    .retrieve()
                    .body(PawnTicketRecord.class);

            if (response == null) {
                throw new IllegalStateException("Pawn ticket service returned no payload");
            }

            return new PawnTicket(
                    "ticket-issued",
                    response.contractNumber(),
                    response.contractBarcode(),
                    response.ticketNumber(),
                    response.termsVersion(),
                    response.termsAndConditionsText(),
                    response.createdAt(),
                    response.dueDate(),
                    response.earliestAuctionDate(),
                    response.termMonths(),
                    response.totalLoanValue(),
                    response.monthlyInterestRate(),
                    response.monthlyOperatingFee(),
                    response.manualMonthlyOperatingFeeRequired(),
                    response.totalInterestAmount(),
                    response.totalOperatingFeeAmount(),
                    response.totalRepaymentAmount(),
                    response.legalText(),
                    response.positions() == null
                            ? List.of()
                            : response.positions().stream()
                                .map(position -> new PawnTicketPosition(
                                        position.itemNumber(),
                                        position.itemBarcode(),
                                        position.label(),
                                        position.description(),
                                        position.pledgedValue()
                                ))
                                .toList()
            );
        } catch (RestClientException exception) {
            throw new IllegalStateException("Pawn ticket service unavailable", exception);
        }
    }

    private record IssueRequest(
            String tenantId,
            String customerId,
            String customerNumber,
            String customerDisplayName,
            String customerPhone,
            BigDecimal loanAmount,
            Integer termMonths,
            BigDecimal manualMonthlyOperatingFee,
            List<PositionPayload> positions
    ) {
    }

    private record PositionPayload(
            String label,
            String description,
            BigDecimal pledgedValue
    ) {
    }

    private record PawnTicketRecord(
            String contractNumber,
            String contractBarcode,
            String ticketNumber,
            String termsVersion,
            String termsAndConditionsText,
            java.time.Instant createdAt,
            java.time.LocalDate dueDate,
            java.time.LocalDate earliestAuctionDate,
            Integer termMonths,
            BigDecimal totalLoanValue,
            BigDecimal monthlyInterestRate,
            BigDecimal monthlyOperatingFee,
            boolean manualMonthlyOperatingFeeRequired,
            BigDecimal totalInterestAmount,
            BigDecimal totalOperatingFeeAmount,
            BigDecimal totalRepaymentAmount,
            String legalText,
            List<PawnTicketPositionRecord> positions
    ) {
    }

    private record PawnTicketPositionRecord(
            String itemNumber,
            String itemBarcode,
            String label,
            String description,
            BigDecimal pledgedValue
    ) {
    }
}
