package io.lombardio.loanorigination.api.http;

import io.lombardio.loanorigination.application.service.CreateLoanCommand;
import io.lombardio.loanorigination.application.service.CreateLoanPositionCommand;
import io.lombardio.loanorigination.application.service.LoanOriginationService;
import io.lombardio.loanorigination.domain.model.LoanCase;
import io.lombardio.loanorigination.domain.model.LoanPosition;
import io.lombardio.loanorigination.domain.model.PawnTicket;
import io.lombardio.loanorigination.domain.model.PawnTicketPosition;
import io.lombardio.loanorigination.domain.model.PledgeRecord;
import io.lombardio.loanorigination.domain.model.ValuationGuideline;
import io.lombardio.platform.security.AuthenticatedUser;
import io.lombardio.loanorigination.infrastructure.security.LoanAuthorizationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}")
public class LoanOriginationController {

    private final LoanOriginationService loanOriginationService;
    private final LoanAuthorizationService authorizationService;

    public LoanOriginationController(
            LoanOriginationService loanOriginationService,
            LoanAuthorizationService authorizationService
    ) {
        this.loanOriginationService = loanOriginationService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/valuation-guidelines")
    public List<ValuationGuidelineResponse> listGuidelines(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String tenantId
    ) {
        authorizationService.requireRead(principal, tenantId);
        return loanOriginationService.listGuidelines(tenantId).stream()
                .map(this::toGuidelineResponse)
                .toList();
    }

    @GetMapping("/loans")
    public List<LoanCaseResponse> listLoans(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String tenantId,
            @org.springframework.web.bind.annotation.RequestParam(name = "customerId", required = false) String customerId
    ) {
        authorizationService.requireRead(principal, tenantId);
        return loanOriginationService.listLoans(tenantId, customerId).stream()
                .map(this::toLoanCaseResponse)
                .toList();
    }

    @PostMapping("/loans")
    public LoanCaseResponse createLoan(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String tenantId,
            @Valid @RequestBody CreateLoanRequest request
    ) {
        authorizationService.requireWrite(principal, tenantId);
        return toLoanCaseResponse(loanOriginationService.createLoan(tenantId, toCommand(request)));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    private CreateLoanCommand toCommand(CreateLoanRequest request) {
        return new CreateLoanCommand(
                request.customerId(),
                request.positions().stream()
                        .map(this::toCommand)
                        .toList(),
                request.termMonths(),
                request.manualMonthlyOperatingFee(),
                request.thirdPartyPledgorPresentation(),
                request.bearerName(),
                request.bearerStreet(),
                request.bearerPostalCode(),
                request.bearerCity(),
                request.powerOfAttorneyDocumentDataUrl()
        );
    }

    private CreateLoanPositionCommand toCommand(PositionPayload request) {
        return new CreateLoanPositionCommand(
                request.ticketGroup(),
                request.label(),
                request.description(),
                request.guidelineId(),
                request.pledgedValue()
        );
    }

    private ValuationGuidelineResponse toGuidelineResponse(ValuationGuideline guideline) {
        return new ValuationGuidelineResponse(
                guideline.id(),
                guideline.category(),
                guideline.material(),
                guideline.label(),
                guideline.description(),
                guideline.baseLoanValue()
        );
    }

    private LoanCaseResponse toLoanCaseResponse(LoanCase loanCase) {
        return new LoanCaseResponse(
                loanCase.id(),
                new CustomerView(
                        loanCase.customer().id(),
                        loanCase.customer().customerNumber(),
                        loanCase.customer().displayName(),
                        loanCase.customer().birthDate(),
                        loanCase.customer().phone(),
                        loanCase.customer().checkedDocumentType()
                ),
                toPledgeRecordResponse(loanCase.pledgeRecord()),
                loanCase.positions().stream().map(this::toPositionResponse).toList(),
                loanCase.pawnTickets().stream().map(this::toPawnTicketResponse).toList()
        );
    }

    private LoanPositionResponse toPositionResponse(LoanPosition position) {
        return new LoanPositionResponse(
                position.id(),
                position.ticketGroup(),
                position.label(),
                position.description(),
                position.guidelineLabel(),
                position.baseLoanValue(),
                position.pledgedValue()
        );
    }

    private PledgeRecordResponse toPledgeRecordResponse(PledgeRecord pledgeRecord) {
        return new PledgeRecordResponse(
                pledgeRecord.id(),
                pledgeRecord.recordedAt(),
                pledgeRecord.languageCode(),
                pledgeRecord.retentionUntil(),
                pledgeRecord.pledgorName(),
                pledgeRecord.pledgorStreet(),
                pledgeRecord.pledgorPostalCode(),
                pledgeRecord.pledgorCity(),
                pledgeRecord.pledgorBirthDate(),
                pledgeRecord.checkedDocumentType(),
                pledgeRecord.powerOfAttorneyRequired(),
                pledgeRecord.bearerName(),
                pledgeRecord.bearerStreet(),
                pledgeRecord.bearerPostalCode(),
                pledgeRecord.bearerCity()
        );
    }

    private PawnTicketResponse toPawnTicketResponse(PawnTicket pawnTicket) {
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
                pawnTicket.totalLoanValue(),
                pawnTicket.monthlyInterestRate(),
                pawnTicket.monthlyOperatingFee(),
                pawnTicket.manualMonthlyOperatingFeeRequired(),
                pawnTicket.totalInterestAmount(),
                pawnTicket.totalOperatingFeeAmount(),
                pawnTicket.totalRepaymentAmount(),
                pawnTicket.legalText(),
                pawnTicket.positions().stream()
                        .map(this::toPawnTicketPositionResponse)
                        .toList()
        );
    }

    private PawnTicketPositionResponse toPawnTicketPositionResponse(PawnTicketPosition position) {
        return new PawnTicketPositionResponse(
                position.itemNumber(),
                position.itemBarcode(),
                position.label(),
                position.description(),
                position.pledgedValue()
        );
    }
}
