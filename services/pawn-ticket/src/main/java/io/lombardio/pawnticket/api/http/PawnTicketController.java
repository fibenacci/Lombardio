package io.lombardio.pawnticket.api.http;

import io.lombardio.pawnticket.application.service.IssuePawnTicketCommand;
import io.lombardio.pawnticket.application.service.PawnTicketQuoteCommand;
import io.lombardio.pawnticket.application.service.PawnTicketDocumentService;
import io.lombardio.pawnticket.application.service.PawnTicketPolicyService;
import io.lombardio.pawnticket.application.service.PawnTicketSettlementCommand;
import io.lombardio.pawnticket.application.service.PawnTicketSettlementResult;
import io.lombardio.pawnticket.domain.model.PawnTicket;
import io.lombardio.pawnticket.domain.model.PawnTicketPosition;
import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.pawnticket.infrastructure.security.PawnTicketAuthorizationService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/pawn-tickets")
public class PawnTicketController {

    private final PawnTicketPolicyService pawnTicketPolicyService;
    private final PawnTicketDocumentService pawnTicketDocumentService;
    private final PawnTicketAuthorizationService authorizationService;

    public PawnTicketController(
            PawnTicketPolicyService pawnTicketPolicyService,
            PawnTicketDocumentService pawnTicketDocumentService,
            PawnTicketAuthorizationService authorizationService
    ) {
        this.pawnTicketPolicyService = pawnTicketPolicyService;
        this.pawnTicketDocumentService = pawnTicketDocumentService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/quote")
    public PawnTicketResponse quote(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody PawnTicketQuoteRequest request
    ) {
        authorizationService.requireTicketWrite(principal);
        return toResponse(pawnTicketPolicyService.quote(toCommand(request)));
    }

    @PostMapping("/issue")
    public PawnTicketResponse issue(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody IssuePawnTicketRequest request
    ) {
        authorizationService.requireTicketWrite(principal, request.tenantId());
        return toResponse(pawnTicketPolicyService.issue(toCommand(request)));
    }

    @PostMapping("/extend")
    public PawnTicketResponse extend(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody ExtendPawnTicketRequest request
    ) {
        authorizationService.requireTicketRead(principal);
        return toResponse(pawnTicketPolicyService.extend(toExtensionCommand(request)));
    }

    @PostMapping("/partial-repayment")
    public SettlementResponse partialRepayment(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody PartialRepaymentRequest request
    ) {
        authorizationService.requireCashRead(principal);
        return toSettlementResponse(pawnTicketPolicyService.settlePartial(toPartialSettlementCommand(request)));
    }

    @PostMapping("/redeem")
    public SettlementResponse redeem(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody RedeemPawnTicketRequest request
    ) {
        authorizationService.requireCashRead(principal);
        return toSettlementResponse(pawnTicketPolicyService.redeem(toRedeemCommand(request)));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping(value = "/{ticketNumber}/document", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> document(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String ticketNumber
    ) {
        PawnTicket pawnTicket = pawnTicketPolicyService.getIssuedTicket(ticketNumber);
        authorizationService.requireTicketRead(principal, pawnTicket.tenantId());
        byte[] pdf = pawnTicketDocumentService.render(pawnTicket);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(ticketNumber + ".pdf")
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/{ticketNumber}/labels", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> labels(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String ticketNumber
    ) {
        PawnTicket pawnTicket = pawnTicketPolicyService.getIssuedTicket(ticketNumber);
        authorizationService.requireTicketRead(principal, pawnTicket.tenantId());
        byte[] pdf = pawnTicketDocumentService.renderLabels(pawnTicket);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(ticketNumber + "-labels.pdf")
                        .build()
                        .toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private PawnTicketResponse toResponse(PawnTicket pawnTicket) {
        return new PawnTicketResponse(
                pawnTicket.contractNumber(),
                pawnTicket.contractBarcode(),
                pawnTicket.ticketNumber(),
                pawnTicket.termsVersion(),
                pawnTicket.termsAndConditionsText(),
                pawnTicket.createdAt(),
                pawnTicket.dueDate(),
                pawnTicket.earliestAuctionDate(),
                pawnTicket.termMonths(),
                pawnTicket.loanAmount(),
                pawnTicket.monthlyInterestRate(),
                pawnTicket.monthlyOperatingFee(),
                pawnTicket.manualMonthlyOperatingFeeRequired(),
                pawnTicket.totalInterestAmount(),
                pawnTicket.totalOperatingFeeAmount(),
                pawnTicket.totalRepaymentAmount(),
                pawnTicket.legalText(),
                pawnTicket.positions().stream()
                        .map(position -> new PawnTicketPositionResponse(
                                position.itemNumber(),
                                position.itemBarcode(),
                                position.label(),
                                position.description(),
                                position.pledgedValue()
                        ))
                        .toList()
        );
    }

    private SettlementResponse toSettlementResponse(PawnTicketSettlementResult settlement) {
        return new SettlementResponse(
                settlement.outstandingLoanAmount(),
                settlement.interestAmount(),
                settlement.operatingFeeAmount(),
                settlement.totalDueAmount(),
                settlement.legalText()
        );
    }

    private PawnTicketPosition toPosition(PawnTicketPositionPayload payload) {
        return new PawnTicketPosition(null, null, payload.label(), payload.description(), payload.pledgedValue());
    }

    private PawnTicketQuoteCommand toCommand(PawnTicketQuoteRequest request) {
        return new PawnTicketQuoteCommand(
                request.loanAmount(),
                request.termMonths(),
                request.manualMonthlyOperatingFee()
        );
    }

    private IssuePawnTicketCommand toCommand(IssuePawnTicketRequest request) {
        return new IssuePawnTicketCommand(
                request.tenantId(),
                request.customerId(),
                request.customerNumber(),
                request.customerDisplayName(),
                request.customerPhone(),
                request.positions().stream().map(this::toPosition).toList(),
                request.loanAmount(),
                request.termMonths(),
                request.manualMonthlyOperatingFee()
        );
    }

    private PawnTicketSettlementCommand toExtensionCommand(ExtendPawnTicketRequest request) {
        return new PawnTicketSettlementCommand(
                request.outstandingLoanAmount(),
                null,
                null,
                request.extensionMonths(),
                request.manualMonthlyOperatingFee()
        );
    }

    private PawnTicketSettlementCommand toPartialSettlementCommand(PartialRepaymentRequest request) {
        return new PawnTicketSettlementCommand(
                request.outstandingLoanAmount(),
                request.repaymentAmount(),
                request.remainingTermMonths(),
                null,
                request.manualMonthlyOperatingFee()
        );
    }

    private PawnTicketSettlementCommand toRedeemCommand(RedeemPawnTicketRequest request) {
        return new PawnTicketSettlementCommand(
                request.outstandingLoanAmount(),
                null,
                request.remainingTermMonths(),
                null,
                request.manualMonthlyOperatingFee()
        );
    }
}
