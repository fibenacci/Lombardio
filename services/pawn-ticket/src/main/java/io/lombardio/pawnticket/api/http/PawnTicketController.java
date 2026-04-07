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
import io.lombardio.pawnticket.application.service.PawnTicketSettlementCommand;
import io.lombardio.pawnticket.domain.model.PawnTicket;
import io.lombardio.pawnticket.infrastructure.security.PawnTicketAuthorizationService;
import io.lombardio.platform.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pawn-tickets")
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed singleton beans")
public class PawnTicketController {

  private final PawnTicketPolicyService pawnTicketPolicyService;
  private final PawnTicketDocumentService pawnTicketDocumentService;
  private final PawnTicketAuthorizationService authorizationService;
  private final ApiMapper mapper;

  @PostMapping("/quote")
  public PawnTicketResponse quote(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @Valid @RequestBody PawnTicketQuoteRequest request) {
    authorizationService.requireTicketWrite(principal);
    return mapper.toPawnTicketResponse(
        pawnTicketPolicyService.quote(mapper.toQuoteCommand(request)));
  }

  @PostMapping("/issue")
  public PawnTicketResponse issue(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @Valid @RequestBody IssuePawnTicketRequest request) {
    authorizationService.requireTicketWrite(principal, request.tenantId());
    return mapper.toPawnTicketResponse(
        pawnTicketPolicyService.issue(mapper.toIssueCommand(request)));
  }

  @PostMapping("/extend")
  public PawnTicketResponse extend(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @Valid @RequestBody ExtendPawnTicketRequest request) {
    authorizationService.requireTicketRead(principal);
    return mapper.toPawnTicketResponse(pawnTicketPolicyService.extend(toExtensionCommand(request)));
  }

  @PostMapping("/partial-repayment")
  public SettlementResponse partialRepayment(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @Valid @RequestBody PartialRepaymentRequest request) {
    authorizationService.requireCashRead(principal);
    return mapper.toSettlementResponse(
        pawnTicketPolicyService.settlePartial(toPartialSettlementCommand(request)));
  }

  @PostMapping("/redeem")
  public SettlementResponse redeem(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @Valid @RequestBody RedeemPawnTicketRequest request) {
    authorizationService.requireCashRead(principal);
    return mapper.toSettlementResponse(pawnTicketPolicyService.redeem(toRedeemCommand(request)));
  }

  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }

  @GetMapping(value = "/{ticketNumber}/document", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> document(
      @AuthenticationPrincipal AuthenticatedUser principal, @PathVariable String ticketNumber) {
    PawnTicket pawnTicket = pawnTicketPolicyService.getIssuedTicket(ticketNumber);
    authorizationService.requireTicketRead(principal, pawnTicket.tenantId());
    byte[] pdf = pawnTicketDocumentService.render(pawnTicket);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.inline().filename(ticketNumber + ".pdf").build().toString())
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf);
  }

  @GetMapping(value = "/{ticketNumber}/labels", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> labels(
      @AuthenticationPrincipal AuthenticatedUser principal, @PathVariable String ticketNumber) {
    PawnTicket pawnTicket = pawnTicketPolicyService.getIssuedTicket(ticketNumber);
    authorizationService.requireTicketRead(principal, pawnTicket.tenantId());
    byte[] pdf = pawnTicketDocumentService.renderLabels(pawnTicket);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.inline().filename(ticketNumber + "-labels.pdf").build().toString())
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf);
  }

  private PawnTicketSettlementCommand toExtensionCommand(ExtendPawnTicketRequest request) {
    return new PawnTicketSettlementCommand(
        request.outstandingLoanAmount(),
        null,
        null,
        request.extensionMonths(),
        request.manualMonthlyOperatingFee());
  }

  private PawnTicketSettlementCommand toPartialSettlementCommand(PartialRepaymentRequest request) {
    return new PawnTicketSettlementCommand(
        request.outstandingLoanAmount(),
        request.repaymentAmount(),
        request.remainingTermMonths(),
        null,
        request.manualMonthlyOperatingFee());
  }

  private PawnTicketSettlementCommand toRedeemCommand(RedeemPawnTicketRequest request) {
    return new PawnTicketSettlementCommand(
        request.outstandingLoanAmount(),
        null,
        request.remainingTermMonths(),
        null,
        request.manualMonthlyOperatingFee());
  }
}
