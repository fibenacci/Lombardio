package io.lombardio.loanorigination.domain.port;

import io.lombardio.loanorigination.domain.model.CustomerProfile;
import io.lombardio.loanorigination.domain.model.LoanPosition;
import io.lombardio.loanorigination.domain.model.PawnTicket;

import java.math.BigDecimal;

public interface PawnTicketIssuer {

    PawnTicket issue(
            String tenantId,
            CustomerProfile customer,
            java.util.List<LoanPosition> positions,
            BigDecimal loanAmount,
            Integer termMonths,
            BigDecimal manualMonthlyOperatingFee
    );
}
