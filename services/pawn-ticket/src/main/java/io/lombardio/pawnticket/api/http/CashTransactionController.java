package io.lombardio.pawnticket.api.http;

import io.lombardio.pawnticket.application.service.CashTransactionService;
import io.lombardio.pawnticket.application.service.ExecuteCashTransactionCommand;
import io.lombardio.pawnticket.domain.model.CashTransaction;
import io.lombardio.pawnticket.infrastructure.security.AuthenticatedPawnTicketUser;
import io.lombardio.pawnticket.infrastructure.security.PawnTicketAuthorizationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CashTransactionController {

    private final CashTransactionService cashTransactionService;
    private final PawnTicketAuthorizationService authorizationService;

    public CashTransactionController(
            CashTransactionService cashTransactionService,
            PawnTicketAuthorizationService authorizationService
    ) {
        this.cashTransactionService = cashTransactionService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/cash-transactions")
    public CashTransactionResponse execute(
            @AuthenticationPrincipal AuthenticatedPawnTicketUser principal,
            @Valid @RequestBody ExecuteCashTransactionRequest request
    ) {
        authorizationService.requireCashWrite(principal, request.tenantId());
        return toResponse(cashTransactionService.execute(toCommand(request)));
    }

    @GetMapping("/tenants/{tenantId}/cash-transactions")
    public List<CashTransactionResponse> list(
            @AuthenticationPrincipal AuthenticatedPawnTicketUser principal,
            @PathVariable String tenantId
    ) {
        authorizationService.requireCashRead(principal, tenantId);
        return cashTransactionService.listTransactions(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ExecuteCashTransactionCommand toCommand(ExecuteCashTransactionRequest request) {
        return new ExecuteCashTransactionCommand(
                request.tenantId(),
                request.ticketNumber(),
                request.type(),
                request.outstandingLoanAmount(),
                request.extensionMonths(),
                request.repaymentAmount(),
                request.remainingTermMonths(),
                request.manualMonthlyOperatingFee(),
                request.note()
        );
    }

    private CashTransactionResponse toResponse(CashTransaction transaction) {
        return new CashTransactionResponse(
                transaction.id(),
                transaction.ticketNumber(),
                transaction.customerNumber(),
                transaction.customerDisplayName(),
                transaction.type(),
                transaction.outstandingLoanAmount(),
                transaction.interestAmount(),
                transaction.operatingFeeAmount(),
                transaction.totalAmount(),
                transaction.legalText(),
                transaction.note(),
                transaction.createdAt()
        );
    }
}
