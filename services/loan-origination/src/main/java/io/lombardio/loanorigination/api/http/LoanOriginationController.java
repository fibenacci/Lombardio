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
package io.lombardio.loanorigination.api.http;

import io.lombardio.loanorigination.api.http.mapper.ApiMapper;
import io.lombardio.loanorigination.application.service.LoanOriginationService;
import io.lombardio.loanorigination.infrastructure.security.LoanAuthorizationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LoanOriginationController {

  private final LoanOriginationService loanOriginationService;
  private final LoanAuthorizationService authorizationService;
  private final ApiMapper mapper;

  @PostMapping("/tenants/{tenantId}/loan-origination/cases")
  public LoanCaseResponse create(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @Valid @RequestBody CreateLoanRequest request) {
    authorizationService.requireWrite(principal, tenantId);
    return mapper.toResponse(
        loanOriginationService.createLoan(tenantId, mapper.toCommand(request)));
  }

  @GetMapping("/tenants/{tenantId}/loan-origination/cases")
  public List<LoanCaseResponse> list(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @RequestParam(required = false) String customerId) {
    authorizationService.requireRead(principal, tenantId);
    return loanOriginationService.listLoans(tenantId, customerId).stream()
        .map(mapper::toResponse)
        .toList();
  }

  @GetMapping("/tenants/{tenantId}/loan-origination/valuation-guidelines")
  public List<ValuationGuidelineResponse> listGuidelines(
      @AuthenticationPrincipal AuthenticatedUser principal, @PathVariable String tenantId) {
    authorizationService.requireRead(principal, tenantId);
    return loanOriginationService.listGuidelines(tenantId).stream()
        .map(mapper::toResponse)
        .toList();
  }
}
