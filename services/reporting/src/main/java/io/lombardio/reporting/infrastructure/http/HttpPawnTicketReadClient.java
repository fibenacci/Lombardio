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

import io.lombardio.reporting.domain.port.PawnTicketReadClient;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpPawnTicketReadClient implements PawnTicketReadClient {

  private final RestClient restClient;

  public HttpPawnTicketReadClient(
      RestClient.Builder restClientBuilder,
      @Value("${pawn-ticket.base-url:http://localhost:8085}") String pawnTicketBaseUrl) {
    this.restClient = restClientBuilder.baseUrl(pawnTicketBaseUrl).build();
  }

  @Override
  public List<ReportedPawnTicketOverview> listTickets(String tenantId, String bearerToken) {
    PawnTicketOverviewProjection[] response =
        restClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder.path("/api/v1/tenants/{tenantId}/pawn-tickets").build(tenantId))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
            .retrieve()
            .body(PawnTicketOverviewProjection[].class);

    if (response == null) {
      return List.of();
    }

    return List.of(response).stream()
        .map(
            item ->
                new ReportedPawnTicketOverview(
                    item.ticketNumber(),
                    item.totalLoanValue(),
                    item.totalRepaymentAmount(),
                    item.positionCount()))
        .toList();
  }

  @Override
  public List<ReportedCashTransaction> listCashTransactions(String tenantId, String bearerToken) {
    CashTransactionProjection[] response =
        restClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder.path("/api/v1/tenants/{tenantId}/cash-transactions").build(tenantId))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
            .retrieve()
            .body(CashTransactionProjection[].class);

    if (response == null) {
      return List.of();
    }

    return List.of(response).stream()
        .map(
            item ->
                new ReportedCashTransaction(
                    item.type(),
                    item.interestAmount(),
                    item.operatingFeeAmount(),
                    item.totalAmount(),
                    item.createdAt()))
        .toList();
  }

  private record PawnTicketOverviewProjection(
      String ticketNumber,
      BigDecimal totalLoanValue,
      BigDecimal totalRepaymentAmount,
      Integer positionCount) {}

  private record CashTransactionProjection(
      String type,
      BigDecimal interestAmount,
      BigDecimal operatingFeeAmount,
      BigDecimal totalAmount,
      Instant createdAt) {}
}
