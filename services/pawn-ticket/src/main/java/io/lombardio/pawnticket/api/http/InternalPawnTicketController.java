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
package io.lombardio.pawnticket.api.http;

import io.lombardio.pawnticket.application.service.PawnTicketDocumentService;
import io.lombardio.pawnticket.application.service.PawnTicketPolicyService;
import io.lombardio.pawnticket.domain.model.PawnTicket;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/v1/customers/{tenantId}/{customerId}/pawn-tickets")
public class InternalPawnTicketController {

  private static final String INTERNAL_AUTH_HEADER = "X-Internal-Service-Token";

  private final PawnTicketPolicyService pawnTicketPolicyService;
  private final PawnTicketDocumentService pawnTicketDocumentService;
  private final String internalServiceToken;

  public InternalPawnTicketController(
      PawnTicketPolicyService pawnTicketPolicyService,
      PawnTicketDocumentService pawnTicketDocumentService,
      @Value("${internal.service-token:dev-internal-token}") String internalServiceToken) {
    this.pawnTicketPolicyService = pawnTicketPolicyService;
    this.pawnTicketDocumentService = pawnTicketDocumentService;
    this.internalServiceToken = internalServiceToken;
  }

  @GetMapping
  public List<PawnTicketOverviewResponse> listCustomerTickets(
      @RequestHeader(name = INTERNAL_AUTH_HEADER, required = false) String requestToken,
      @PathVariable String tenantId,
      @PathVariable String customerId) {
    requireInternalToken(requestToken);
    return pawnTicketPolicyService.listIssuedTickets(tenantId, customerId).stream()
        .map(this::toOverview)
        .toList();
  }

  @GetMapping(value = "/{ticketNumber}/document", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> document(
      @RequestHeader(name = INTERNAL_AUTH_HEADER, required = false) String requestToken,
      @PathVariable String tenantId,
      @PathVariable String customerId,
      @PathVariable String ticketNumber) {
    requireInternalToken(requestToken);
    PawnTicket pawnTicket = pawnTicketPolicyService.getIssuedTicket(ticketNumber);
    if (!tenantId.equals(pawnTicket.tenantId()) || !customerId.equals(pawnTicket.customerId())) {
      throw new AccessDeniedException("Ticket access denied");
    }

    byte[] pdf = pawnTicketDocumentService.render(pawnTicket);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.inline().filename(ticketNumber + ".pdf").build().toString())
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf);
  }

  private PawnTicketOverviewResponse toOverview(PawnTicket pawnTicket) {
    return new PawnTicketOverviewResponse(
        pawnTicket.contractNumber(),
        pawnTicket.ticketNumber(),
        pawnTicket.contractBarcode(),
        pawnTicket.termsVersion(),
        pawnTicket.customerNumber(),
        pawnTicket.customerDisplayName(),
        pawnTicket.createdAt(),
        pawnTicket.dueDate(),
        pawnTicket.earliestAuctionDate(),
        pawnTicket.loanAmount(),
        pawnTicket.totalRepaymentAmount(),
        pawnTicket.positions().size());
  }

  private void requireInternalToken(String requestToken) {
    if (requestToken == null || !requestToken.equals(internalServiceToken)) {
      throw new AccessDeniedException("Internal service token required");
    }
  }
}
