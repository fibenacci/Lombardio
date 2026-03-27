package io.lombardio.identity.portal.application;

import io.lombardio.identity.domain.model.Customer;
import io.lombardio.identity.domain.port.CustomerRepository;
import io.lombardio.identity.portal.api.CustomerPortalAcceptInvitationRequest;
import io.lombardio.identity.portal.api.CustomerPortalInvitationResponse;
import io.lombardio.identity.portal.api.CustomerPortalLoginRequest;
import io.lombardio.identity.portal.api.CustomerPortalLoginResponse;
import io.lombardio.identity.portal.api.CustomerPortalMeResponse;
import io.lombardio.identity.portal.api.CustomerPortalPawnTicketResponse;
import io.lombardio.identity.portal.infrastructure.notification.CustomerPortalNotificationSender;
import io.lombardio.identity.portal.infrastructure.persistence.CustomerPortalCredentialEntity;
import io.lombardio.identity.portal.infrastructure.persistence.CustomerPortalInvitationEntity;
import io.lombardio.identity.portal.infrastructure.persistence.CustomerPortalSessionEntity;
import io.lombardio.identity.portal.infrastructure.persistence.CustomerPortalCredentialRepository;
import io.lombardio.identity.portal.infrastructure.persistence.CustomerPortalInvitationRepository;
import io.lombardio.identity.portal.infrastructure.persistence.CustomerPortalSessionRepository;
import io.lombardio.identity.portal.infrastructure.security.AuthenticatedCustomerPortalUser;
import io.lombardio.identity.portal.infrastructure.ticket.CustomerPortalTicketClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerPortalService {

    private final CustomerRepository customerRepository;
    private final CustomerPortalCredentialRepository credentialRepository;
    private final CustomerPortalInvitationRepository invitationRepository;
    private final CustomerPortalSessionRepository sessionRepository;
    private final CustomerPortalTicketClient ticketClient;
    private final CustomerPortalNotificationSender notificationSender;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public CustomerPortalService(
            CustomerRepository customerRepository,
            CustomerPortalCredentialRepository credentialRepository,
            CustomerPortalInvitationRepository invitationRepository,
            CustomerPortalSessionRepository sessionRepository,
            CustomerPortalTicketClient ticketClient,
            CustomerPortalNotificationSender notificationSender,
            PasswordEncoder passwordEncoder,
            Clock clock
    ) {
        this.customerRepository = customerRepository;
        this.credentialRepository = credentialRepository;
        this.invitationRepository = invitationRepository;
        this.sessionRepository = sessionRepository;
        this.ticketClient = ticketClient;
        this.notificationSender = notificationSender;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional
    public void issueInvitation(Customer customer) {
        if (!customer.wantsDigitalPawnTicket() || customer.email() == null || customer.email().isBlank()) {
            return;
        }

        invitationRepository.deleteByCustomerIdAndUsedAtIsNull(customer.id());

        CustomerPortalInvitationEntity invitation = new CustomerPortalInvitationEntity();
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setCustomerId(customer.id());
        invitation.setTenantId(customer.tenantId());
        invitation.setEmail(customer.email());
        invitation.setIssuedAt(Instant.now(clock));
        invitation.setExpiresAt(Instant.now(clock).plus(7, ChronoUnit.DAYS));
        invitationRepository.save(invitation);

        notificationSender.sendInvitation(customer, invitation.getToken(), invitation.getExpiresAt());
    }

    @Transactional
    public void disableAccess(Customer customer) {
        sessionRepository.deleteByCustomerId(customer.id());
        invitationRepository.deleteByCustomerIdAndUsedAtIsNull(customer.id());
    }

    @Transactional(readOnly = true)
    public CustomerPortalInvitationResponse getInvitation(String token) {
        CustomerPortalInvitationEntity invitation = requireUsableInvitation(token);
        Customer customer = requireCustomer(invitation.getCustomerId());
        return new CustomerPortalInvitationResponse(customer.displayName(), invitation.getEmail(), customer.onlineAccessStatus());
    }

    @Transactional
    public CustomerPortalLoginResponse acceptInvitation(CustomerPortalAcceptInvitationRequest request) {
        CustomerPortalInvitationEntity invitation = requireUsableInvitation(request.token());
        Customer customer = requireCustomer(invitation.getCustomerId());

        CustomerPortalCredentialEntity credential = credentialRepository.findById(customer.id())
                .orElseGet(CustomerPortalCredentialEntity::new);
        credential.setCustomerId(customer.id());
        credential.setPasswordHash(passwordEncoder.encode(request.password()));
        credential.setActivatedAt(Instant.now(clock));
        credentialRepository.save(credential);

        invitation.setUsedAt(Instant.now(clock));
        invitationRepository.save(invitation);

        Customer activated = activateCustomer(customer);
        CustomerPortalSessionEntity session = issueSession(activated);
        return new CustomerPortalLoginResponse(session.getToken(), toMeResponse(activated));
    }

    @Transactional
    public CustomerPortalLoginResponse login(CustomerPortalLoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        Customer customer = customerRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!"ACTIVE".equals(customer.onlineAccessStatus()) || !customer.wantsDigitalPawnTicket()) {
            throw new IllegalArgumentException("Customer portal access is not active");
        }

        CustomerPortalCredentialEntity credential = credentialRepository.findById(customer.id())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        CustomerPortalSessionEntity session = issueSession(customer);
        return new CustomerPortalLoginResponse(session.getToken(), toMeResponse(customer));
    }

    @Transactional(readOnly = true)
    public CustomerPortalMeResponse currentCustomer(AuthenticatedCustomerPortalUser principal) {
        return toMeResponse(requireActiveCustomer(principal.customerId()));
    }

    @Transactional(readOnly = true)
    public List<CustomerPortalPawnTicketResponse> listPawnTickets(AuthenticatedCustomerPortalUser principal) {
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

        CustomerPortalSessionEntity session = sessionRepository.findById(token).orElse(null);
        if (session == null) {
            return null;
        }

        try {
            Customer customer = requireActiveCustomer(session.getCustomerId());
            return new AuthenticatedCustomerPortalUser(customer.id(), customer.tenantId(), customer.displayName(), customer.email());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private CustomerPortalSessionEntity issueSession(Customer customer) {
        CustomerPortalSessionEntity session = new CustomerPortalSessionEntity();
        session.setToken(UUID.randomUUID().toString());
        session.setCustomerId(customer.id());
        session.setTenantId(customer.tenantId());
        session.setIssuedAt(Instant.now(clock));
        return sessionRepository.save(session);
    }

    private Customer activateCustomer(Customer customer) {
        Customer updated = new Customer(
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
                customer.city()
        );
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
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    private CustomerPortalInvitationEntity requireUsableInvitation(String token) {
        CustomerPortalInvitationEntity invitation = invitationRepository.findById(token)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (invitation.getUsedAt() != null) {
            throw new IllegalArgumentException("Invitation already used");
        }
        if (invitation.getExpiresAt().isBefore(Instant.now(clock))) {
            throw new IllegalArgumentException("Invitation expired");
        }
        return invitation;
    }

    private CustomerPortalMeResponse toMeResponse(Customer customer) {
        return new CustomerPortalMeResponse(
                customer.id(),
                customer.tenantId(),
                customer.displayName(),
                customer.email(),
                customer.onlineAccessStatus()
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
