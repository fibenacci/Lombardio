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
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public final class LoanOriginationController {

  private static final Logger log = LoggerFactory.getLogger(LoanOriginationController.class);

  private final LoanOriginationService loanOriginationService;
  private final LoanAuthorizationService authorizationService;
  private final ApiMapper mapper;

  public LoanOriginationController(
      LoanOriginationService loanOriginationService,
      LoanAuthorizationService authorizationService,
      ApiMapper mapper) {
    this.loanOriginationService = Objects.requireNonNull(loanOriginationService);
    this.authorizationService = Objects.requireNonNull(authorizationService);
    this.mapper = Objects.requireNonNull(mapper);
    log.debug("Initialized with mapper: {}", mapper.getClass().getSimpleName());
  }

  @PostMapping("/tenants/{tenantId}/loan-origination/cases")
  public LoanCaseResponse create(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @Valid @RequestBody CreateLoanRequest request) {
    this.authorizationService.requireWrite(principal, tenantId);
    return this.mapper.toResponse(
        this.loanOriginationService.createLoan(tenantId, this.mapper.toCommand(request)));
  }

  @GetMapping("/tenants/{tenantId}/loan-origination/cases")
  public List<LoanCaseResponse> list(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable String tenantId,
      @RequestParam(required = false) String customerId) {
    this.authorizationService.requireRead(principal, tenantId);
    return this.loanOriginationService.listLoans(tenantId, customerId).stream()
        .map(loan -> this.mapper.toResponse(loan))
        .toList();
  }

  @GetMapping("/tenants/{tenantId}/loan-origination/valuation-guidelines")
  public List<ValuationGuidelineResponse> listGuidelines(
      @AuthenticationPrincipal AuthenticatedUser principal, @PathVariable String tenantId) {
    this.authorizationService.requireRead(principal, tenantId);
    return this.loanOriginationService.listGuidelines(tenantId).stream()
        .map(guideline -> this.mapper.toResponse(guideline))
        .toList();
  }
}
