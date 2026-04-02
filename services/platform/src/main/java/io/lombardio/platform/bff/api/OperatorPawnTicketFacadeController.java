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
package io.lombardio.platform.bff.api;

import io.lombardio.platform.bff.application.OperatorBffProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform/operator")
public class OperatorPawnTicketFacadeController extends OperatorFacadeSupport {
  public OperatorPawnTicketFacadeController(OperatorBffProxyService proxyService) {
    super(proxyService);
  }

  @PostMapping("/pawn-tickets/quote")
  public ResponseEntity<byte[]> quotePawnTicket(
      HttpServletRequest request, @RequestBody(required = false) byte[] body) {
    return forwardPost("pawn-ticket", request, "/api/v1/pawn-tickets/quote", body);
  }

  @GetMapping("/tenants/{tenantId}/pawn-tickets")
  public ResponseEntity<byte[]> listPawnTickets(
      @PathVariable String tenantId, HttpServletRequest request) {
    return forwardGet("pawn-ticket", request, "/api/v1/tenants/" + tenantId + "/pawn-tickets");
  }

  @GetMapping("/pawn-tickets/{ticketNumber}/document")
  public ResponseEntity<byte[]> getPawnTicketDocument(
      @PathVariable String ticketNumber, HttpServletRequest request) {
    return forwardGet("pawn-ticket", request, "/api/v1/pawn-tickets/" + ticketNumber + "/document");
  }

  @GetMapping("/pawn-tickets/{ticketNumber}/labels")
  public ResponseEntity<byte[]> getPawnTicketLabels(
      @PathVariable String ticketNumber, HttpServletRequest request) {
    return forwardGet("pawn-ticket", request, "/api/v1/pawn-tickets/" + ticketNumber + "/labels");
  }

  @PostMapping("/pawn-tickets/extend")
  public ResponseEntity<byte[]> extendPawnTicket(
      HttpServletRequest request, @RequestBody(required = false) byte[] body) {
    return forwardPost("pawn-ticket", request, "/api/v1/pawn-tickets/extend", body);
  }

  @PostMapping("/pawn-tickets/redeem")
  public ResponseEntity<byte[]> redeemPawnTicket(
      HttpServletRequest request, @RequestBody(required = false) byte[] body) {
    return forwardPost("pawn-ticket", request, "/api/v1/pawn-tickets/redeem", body);
  }

  @PostMapping("/pawn-tickets/partial-repayment")
  public ResponseEntity<byte[]> calculatePartialRepayment(
      HttpServletRequest request, @RequestBody(required = false) byte[] body) {
    return forwardPost("pawn-ticket", request, "/api/v1/pawn-tickets/partial-repayment", body);
  }

  @PostMapping("/cash-transactions")
  public ResponseEntity<byte[]> createCashTransaction(
      HttpServletRequest request, @RequestBody(required = false) byte[] body) {
    return forwardPost("pawn-ticket", request, "/api/v1/cash-transactions", body);
  }

  @GetMapping("/tenants/{tenantId}/cash-transactions")
  public ResponseEntity<byte[]> listCashTransactions(
      @PathVariable String tenantId, HttpServletRequest request) {
    return forwardGet("pawn-ticket", request, "/api/v1/tenants/" + tenantId + "/cash-transactions");
  }
}
