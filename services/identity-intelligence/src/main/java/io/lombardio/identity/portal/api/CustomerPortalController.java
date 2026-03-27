package io.lombardio.identity.portal.api;

import io.lombardio.identity.portal.application.CustomerPortalService;
import io.lombardio.identity.portal.infrastructure.security.AuthenticatedCustomerPortalUser;
import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer-portal")
public class CustomerPortalController {

    private final CustomerPortalService customerPortalService;

    public CustomerPortalController(CustomerPortalService customerPortalService) {
        this.customerPortalService = customerPortalService;
    }

    @GetMapping("/invitations/{token}")
    public CustomerPortalInvitationResponse invitation(@PathVariable String token) {
        return customerPortalService.getInvitation(token);
    }

    @PostMapping("/invitations/accept")
    public CustomerPortalLoginResponse acceptInvitation(@Valid @RequestBody CustomerPortalAcceptInvitationRequest request) {
        return customerPortalService.acceptInvitation(request);
    }

    @PostMapping("/auth/login")
    public CustomerPortalLoginResponse login(@Valid @RequestBody CustomerPortalLoginRequest request) {
        return customerPortalService.login(request);
    }

    @GetMapping("/auth/me")
    public CustomerPortalMeResponse me(@AuthenticationPrincipal AuthenticatedCustomerPortalUser principal) {
        return customerPortalService.currentCustomer(principal);
    }

    @GetMapping("/pawn-tickets")
    public List<CustomerPortalPawnTicketResponse> pawnTickets(
            @AuthenticationPrincipal AuthenticatedCustomerPortalUser principal
    ) {
        return customerPortalService.listPawnTickets(principal);
    }

    @GetMapping(value = "/pawn-tickets/{ticketNumber}/document", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> document(
            @AuthenticationPrincipal AuthenticatedCustomerPortalUser principal,
            @PathVariable String ticketNumber
    ) {
        byte[] pdf = customerPortalService.downloadDocument(principal, ticketNumber);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(ticketNumber + ".pdf")
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
