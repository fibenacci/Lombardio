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
import io.lombardio.pawnticket.application.service.CashTransactionService;
import io.lombardio.pawnticket.infrastructure.security.PawnTicketAuthorizationService;
import io.lombardio.platform.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed singleton beans")
public class CashTransactionController {

  private final CashTransactionService cashTransactionService;
  private final PawnTicketAuthorizationService authorizationService;
  private final ApiMapper mapper;

  @PostMapping("/cash-transactions")
  public CashTransactionResponse execute(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @Valid @RequestBody ExecuteCashTransactionRequest request) {
    authorizationService.requireCashWrite(principal, request.tenantId());
    return mapper.toCashTransactionResponse(
        cashTransactionService.execute(mapper.toExecuteCashTransactionCommand(request)));
  }

  @GetMapping("/tenants/{tenantId}/cash-transactions")
  public List<CashTransactionResponse> list(
      @AuthenticationPrincipal AuthenticatedUser principal, @PathVariable String tenantId) {
    authorizationService.requireCashRead(principal, tenantId);
    return cashTransactionService.listTransactions(tenantId).stream()
        .map(mapper::toCashTransactionResponse)
        .toList();
  }
}
