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
package io.lombardio.identity.portal.api;

import io.lombardio.identity.config.CustomerPortalSessionProperties;
import io.lombardio.identity.portal.application.AuthenticatedCustomerPortalUser;
import io.lombardio.identity.portal.application.CustomerPortalAcceptInvitationCommand;
import io.lombardio.identity.portal.application.CustomerPortalCustomerView;
import io.lombardio.identity.portal.application.CustomerPortalInvitationView;
import io.lombardio.identity.portal.application.CustomerPortalLoginCommand;
import io.lombardio.identity.portal.application.CustomerPortalLoginView;
import io.lombardio.identity.portal.application.CustomerPortalPawnTicketView;
import io.lombardio.identity.portal.application.CustomerPortalService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer-portal")
public class CustomerPortalController {

  private final CustomerPortalService customerPortalService;
  private final CustomerPortalSessionProperties sessionProperties;

  public CustomerPortalController(
      CustomerPortalService customerPortalService,
      CustomerPortalSessionProperties sessionProperties) {
    this.customerPortalService = customerPortalService;
    this.sessionProperties = sessionProperties;
  }

  @PostMapping("/invitations/lookup")
  public CustomerPortalInvitationView invitation(
      @Valid @RequestBody CustomerPortalInvitationLookupRequest request) {
    return customerPortalService.getInvitation(request.token());
  }

  @PostMapping("/invitations/accept")
  public CustomerPortalLoginView acceptInvitation(
      @Valid @RequestBody CustomerPortalAcceptInvitationRequest request,
      HttpServletResponse response) {
    CustomerPortalLoginView session =
        customerPortalService.acceptInvitation(
            new CustomerPortalAcceptInvitationCommand(request.token(), request.password()));
    writeSessionCookie(response, session.accessToken());
    return cookieOnlySession(session);
  }

  @PostMapping("/auth/login")
  public CustomerPortalLoginView login(
      @Valid @RequestBody CustomerPortalLoginRequest request, HttpServletResponse response) {
    CustomerPortalLoginView session =
        customerPortalService.login(
            new CustomerPortalLoginCommand(request.email(), request.password()));
    writeSessionCookie(response, session.accessToken());
    return cookieOnlySession(session);
  }

  @PostMapping("/auth/refresh")
  public ResponseEntity<CustomerPortalLoginView> refresh(
      HttpServletRequest request, HttpServletResponse response) {
    String token = readSessionToken(request);
    if (token == null || token.isBlank()) {
      return ResponseEntity.noContent().build();
    }

    CustomerPortalLoginView session = customerPortalService.refresh(token);
    if (session == null) {
      clearSessionCookie(response);
      return ResponseEntity.noContent().build();
    }

    writeSessionCookie(response, session.accessToken());
    return ResponseEntity.ok(cookieOnlySession(session));
  }

  @PostMapping("/auth/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    customerPortalService.logout(readSessionToken(request));
    clearSessionCookie(response);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/auth/me")
  public CustomerPortalCustomerView me(
      @AuthenticationPrincipal AuthenticatedCustomerPortalUser principal) {
    return customerPortalService.currentCustomer(principal);
  }

  @GetMapping("/pawn-tickets")
  public List<CustomerPortalPawnTicketView> pawnTickets(
      @AuthenticationPrincipal AuthenticatedCustomerPortalUser principal) {
    return customerPortalService.listPawnTickets(principal);
  }

  @GetMapping(
      value = "/pawn-tickets/{ticketNumber}/document",
      produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> document(
      @AuthenticationPrincipal AuthenticatedCustomerPortalUser principal,
      @PathVariable String ticketNumber) {
    byte[] pdf = customerPortalService.downloadDocument(principal, ticketNumber);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.inline().filename(ticketNumber + ".pdf").build().toString())
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf);
  }

  private void writeSessionCookie(HttpServletResponse response, String token) {
    response.addHeader(
        HttpHeaders.SET_COOKIE,
        buildSessionCookie(token, sessionProperties.cookieMaxAgeSeconds()).toString());
  }

  private void clearSessionCookie(HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE, buildSessionCookie("", 0).toString());
  }

  private ResponseCookie buildSessionCookie(String value, long maxAgeSeconds) {
    return ResponseCookie.from(sessionProperties.cookieName(), value)
        .httpOnly(true)
        .secure(sessionProperties.cookieSecure())
        .sameSite(sessionProperties.cookieSameSite())
        .path(sessionProperties.cookiePath())
        .maxAge(maxAgeSeconds)
        .build();
  }

  private String readSessionToken(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }

    for (Cookie cookie : request.getCookies()) {
      if (sessionProperties.cookieName().equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }

  private CustomerPortalLoginView cookieOnlySession(CustomerPortalLoginView session) {
    return new CustomerPortalLoginView(null, session.customer());
  }
}
