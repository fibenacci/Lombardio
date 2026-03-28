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
package io.lombardio.reporting.infrastructure.http;

import io.lombardio.reporting.domain.port.LoanReadClient;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpLoanReadClient implements LoanReadClient {

  private final RestClient restClient;

  public HttpLoanReadClient(
      RestClient.Builder restClientBuilder,
      @Value("${loan-origination.base-url:http://localhost:8083}") String loanOriginationBaseUrl) {
    this.restClient = restClientBuilder.baseUrl(loanOriginationBaseUrl).build();
  }

  @Override
  public List<ReportedLoanCase> listLoans(String tenantId, String bearerToken) {
    LoanCaseProjection[] response =
        restClient
            .get()
            .uri(uriBuilder -> uriBuilder.path("/api/v1/tenants/{tenantId}/loans").build(tenantId))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
            .retrieve()
            .body(LoanCaseProjection[].class);

    if (response == null) {
      return List.of();
    }

    return List.of(response).stream()
        .map(
            item ->
                new ReportedLoanCase(
                    item.id(),
                    item.pledgeRecord() == null ? null : item.pledgeRecord().recordedAt(),
                    item.positions() == null
                        ? List.of()
                        : item.positions().stream()
                            .map(
                                position ->
                                    new ReportedLoanPosition(
                                        position.label(),
                                        position.guidelineLabel(),
                                        position.pledgedValue()))
                            .toList(),
                    item.pawnTickets() == null
                        ? List.of()
                        : item.pawnTickets().stream()
                            .map(
                                ticket ->
                                    new ReportedPawnTicket(
                                        ticket.ticketNumber(), ticket.totalLoanValue()))
                            .toList()))
        .toList();
  }

  private record LoanCaseProjection(
      String id,
      PledgeRecordProjection pledgeRecord,
      List<LoanPositionProjection> positions,
      List<PawnTicketProjection> pawnTickets) {}

  private record PledgeRecordProjection(Instant recordedAt) {}

  private record LoanPositionProjection(
      String label, String guidelineLabel, java.math.BigDecimal pledgedValue) {}

  private record PawnTicketProjection(String ticketNumber, java.math.BigDecimal totalLoanValue) {}
}
