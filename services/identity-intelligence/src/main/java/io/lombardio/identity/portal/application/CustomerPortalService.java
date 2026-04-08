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
package io.lombardio.identity.portal.application;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.identity.config.CustomerPortalSessionProperties;
import io.lombardio.identity.domain.model.Customer;
import io.lombardio.identity.domain.port.CustomerRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerPortalService {

  private final CustomerRepository customerRepository;
  private final CustomerPortalCredentialStore credentialStore;
  private final CustomerPortalInvitationStore invitationStore;
  private final CustomerPortalSessionStore sessionStore;
  private final CustomerPortalTicketClient ticketClient;
  private final CustomerPortalNotificationSender notificationSender;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;
  private final CustomerPortalSessionProperties sessionProperties;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification =
          "Spring-managed repositories are shared infrastructure dependencies and cannot be defensively copied")
  public CustomerPortalService(
      CustomerRepository customerRepository,
      CustomerPortalCredentialStore credentialStore,
      CustomerPortalInvitationStore invitationStore,
      CustomerPortalSessionStore sessionStore,
      CustomerPortalTicketClient ticketClient,
      CustomerPortalNotificationSender notificationSender,
      PasswordEncoder passwordEncoder,
      Clock clock,
      CustomerPortalSessionProperties sessionProperties) {
    this.customerRepository = customerRepository;
    this.credentialStore = credentialStore;
    this.invitationStore = invitationStore;
    this.sessionStore = sessionStore;
    this.ticketClient = ticketClient;
    this.notificationSender = notificationSender;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
    this.sessionProperties = sessionProperties;
  }

  @Transactional
  public void issueInvitation(Customer customer) {
    if (!customer.wantsDigitalPawnTicket()
        || customer.email() == null
        || customer.email().isBlank()) {
      return;
    }

    invitationStore.deleteUnusedByCustomerId(customer.id());

    String invitationToken = UUID.randomUUID().toString();
    CustomerPortalInvitation invitation =
        invitationStore.save(
            new CustomerPortalInvitation(
                UUID.randomUUID().toString(),
                CustomerPortalTokenHasher.sha256(invitationToken),
                customer.id(),
                customer.tenantId(),
                customer.email(),
                Instant.now(clock),
                Instant.now(clock).plus(7, ChronoUnit.DAYS),
                null));

    notificationSender.sendInvitation(customer, invitationToken, invitation.expiresAt());
  }

  @Transactional
  public void disableAccess(Customer customer) {
    sessionStore.deleteByCustomerId(customer.id());
    invitationStore.deleteUnusedByCustomerId(customer.id());
  }

  @Transactional(readOnly = true)
  public CustomerPortalInvitationView getInvitation(String token) {
    CustomerPortalInvitation invitation = requireUsableInvitation(token);
    Customer customer = requireCustomer(invitation.customerId());
    return new CustomerPortalInvitationView(
        customer.displayName(), invitation.email(), customer.onlineAccessStatus());
  }

  @Transactional
  public CustomerPortalLoginView acceptInvitation(CustomerPortalAcceptInvitationCommand request) {
    CustomerPortalInvitation invitation = requireUsableInvitation(request.token());
    Customer customer = requireCustomer(invitation.customerId());

    credentialStore.save(
        new CustomerPortalCredential(
            customer.id(), passwordEncoder.encode(request.password()), Instant.now(clock)));

    invitationStore.save(
        new CustomerPortalInvitation(
            invitation.token(),
            invitation.tokenHash(),
            invitation.customerId(),
            invitation.tenantId(),
            invitation.email(),
            invitation.issuedAt(),
            invitation.expiresAt(),
            Instant.now(clock)));

    Customer activated = activateCustomer(customer);
    return new CustomerPortalLoginView(issueSessionToken(activated), toMeResponse(activated));
  }

  @Transactional
  public CustomerPortalLoginView login(CustomerPortalLoginCommand request) {
    String normalizedEmail = normalizeEmail(request.email());
    Customer customer =
        customerRepository
            .findByEmail(normalizedEmail)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

    if (!"ACTIVE".equals(customer.onlineAccessStatus()) || !customer.wantsDigitalPawnTicket()) {
      throw new IllegalArgumentException("Customer portal access is not active");
    }

    CustomerPortalCredential credential =
        credentialStore
            .findByCustomerId(customer.id())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

    if (!passwordEncoder.matches(request.password(), credential.passwordHash())) {
      throw new IllegalArgumentException("Invalid email or password");
    }

    return new CustomerPortalLoginView(issueSessionToken(customer), toMeResponse(customer));
  }

  @Transactional(readOnly = true)
  public CustomerPortalCustomerView currentCustomer(AuthenticatedCustomerPortalUser principal) {
    return toMeResponse(requireActiveCustomer(principal.customerId()));
  }

  @Transactional(readOnly = true)
  public CustomerPortalLoginView refresh(String token) {
    AuthenticatedCustomerPortalUser principal = authenticate(token);
    if (principal == null) {
      return null;
    }

    sessionStore.deleteByToken(principal.sessionToken());
    Customer customer = requireActiveCustomer(principal.customerId());
    return new CustomerPortalLoginView(issueSessionToken(customer), toMeResponse(customer));
  }

  @Transactional(readOnly = true)
  public List<CustomerPortalPawnTicketView> listPawnTickets(
      AuthenticatedCustomerPortalUser principal) {
    Customer customer = requireActiveCustomer(principal.customerId());
    return ticketClient.listTickets(customer.tenantId(), customer.id());
  }

  @Transactional(readOnly = true)
  public byte[] downloadDocument(AuthenticatedCustomerPortalUser principal, String ticketNumber) {
    Customer customer = requireActiveCustomer(principal.customerId());
    return ticketClient.downloadDocument(customer.tenantId(), customer.id(), ticketNumber);
  }

  @Transactional(readOnly = true)
  public AuthenticatedCustomerPortalUser authenticate(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }

    CustomerPortalSession session = findSessionByToken(token);
    if (session == null) {
      return null;
    }
    if (session.expiresAt() == null || session.expiresAt().isBefore(Instant.now(clock))) {
      sessionStore.deleteByToken(session.token());
      return null;
    }

    try {
      Customer customer = requireActiveCustomer(session.customerId());
      return new AuthenticatedCustomerPortalUser(
          customer.id(),
          customer.tenantId(),
          customer.displayName(),
          customer.email(),
          session.token());
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }

  @Transactional
  public void logout(String token) {
    if (token == null || token.isBlank()) {
      return;
    }
    CustomerPortalSession session = findSessionByToken(token);
    if (session != null) {
      sessionStore.deleteByToken(session.token());
    }
  }

  private String issueSessionToken(Customer customer) {
    String rawToken = UUID.randomUUID().toString();
    sessionStore.save(
        new CustomerPortalSession(
            UUID.randomUUID().toString(),
            CustomerPortalTokenHasher.sha256(rawToken),
            customer.id(),
            customer.tenantId(),
            Instant.now(clock),
            Instant.now(clock).plus(sessionProperties.sessionTtlSeconds(), ChronoUnit.SECONDS)));
    return rawToken;
  }

  private Customer activateCustomer(Customer customer) {
    Customer updated =
        new Customer(
            customer.id(),
            customer.tenantId(),
            customer.customerNumber(),
            customer.firstName(),
            customer.lastName(),
            customer.birthDate(),
            customer.phone(),
            customer.email(),
            true,
            "ACTIVE",
            customer.street(),
            customer.postalCode(),
            customer.city());
    return customerRepository.save(updated);
  }

  private Customer requireActiveCustomer(String customerId) {
    Customer customer = requireCustomer(customerId);
    if (!customer.wantsDigitalPawnTicket() || !"ACTIVE".equals(customer.onlineAccessStatus())) {
      throw new IllegalArgumentException("Customer portal access is not active");
    }
    return customer;
  }

  private Customer requireCustomer(String customerId) {
    return customerRepository
        .findById(customerId)
        .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
  }

  private CustomerPortalInvitation requireUsableInvitation(String token) {
    CustomerPortalInvitation invitation = findInvitationByToken(token);

    if (invitation.usedAt() != null) {
      throw new IllegalArgumentException("Invitation already used");
    }
    if (invitation.expiresAt().isBefore(Instant.now(clock))) {
      throw new IllegalArgumentException("Invitation expired");
    }
    return invitation;
  }

  private CustomerPortalCustomerView toMeResponse(Customer customer) {
    return new CustomerPortalCustomerView(
        customer.id(),
        customer.tenantId(),
        customer.displayName(),
        customer.email(),
        customer.onlineAccessStatus());
  }

  private String normalizeEmail(String email) {
    return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
  }

  private CustomerPortalInvitation findInvitationByToken(String token) {
    // Legacy fallback for invitations issued before token hashing was introduced.
    return invitationStore
        .findByTokenHash(CustomerPortalTokenHasher.sha256(token))
        .or(() -> invitationStore.findByToken(token))
        .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
  }

  private CustomerPortalSession findSessionByToken(String token) {
    // Legacy fallback for sessions issued before token hashing was introduced.
    return sessionStore
        .findByTokenHash(CustomerPortalTokenHasher.sha256(token))
        .or(() -> sessionStore.findByToken(token))
        .orElse(null);
  }
}
