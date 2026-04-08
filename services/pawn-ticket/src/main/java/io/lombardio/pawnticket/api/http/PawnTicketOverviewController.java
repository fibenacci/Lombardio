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
import io.lombardio.pawnticket.application.service.PawnTicketPolicyService;
import io.lombardio.pawnticket.infrastructure.security.PawnTicketAuthorizationService;
import io.lombardio.platform.security.AuthenticatedUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/pawn-tickets")
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed singleton beans")
public class PawnTicketOverviewController {

  private final PawnTicketPolicyService pawnTicketPolicyService;
  private final PawnTicketAuthorizationService authorizationService;
  private final ApiMapper mapper;

  @GetMapping
  public List<PawnTicketOverviewResponse> listTickets(
      @AuthenticationPrincipal AuthenticatedUser principal, @PathVariable String tenantId) {
    authorizationService.requireTicketRead(principal, tenantId);
    return pawnTicketPolicyService.listIssuedTickets(tenantId).stream()
        .map(mapper::toOverviewResponse)
        .toList();
  }
}
