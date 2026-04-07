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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.pawnticket.api.http.mapper.ApiMapper;
import io.lombardio.pawnticket.application.service.PawnTicketDocumentService;
import io.lombardio.pawnticket.application.service.PawnTicketPolicyService;
import io.lombardio.pawnticket.domain.model.PawnTicket;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/v1/customers/{tenantId}/{customerId}/pawn-tickets")
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed singleton beans")
public class InternalPawnTicketController {

  private final PawnTicketPolicyService pawnTicketPolicyService;
  private final PawnTicketDocumentService pawnTicketDocumentService;
  private final ApiMapper mapper;

  @GetMapping
  public List<PawnTicketOverviewResponse> listCustomerTickets(
      @PathVariable String tenantId, @PathVariable String customerId) {
    return pawnTicketPolicyService.listIssuedTickets(tenantId, customerId).stream()
        .map(mapper::toOverviewResponse)
        .toList();
  }

  @GetMapping(value = "/{ticketNumber}/document", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> document(
      @PathVariable String tenantId,
      @PathVariable String customerId,
      @PathVariable String ticketNumber) {
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
}
