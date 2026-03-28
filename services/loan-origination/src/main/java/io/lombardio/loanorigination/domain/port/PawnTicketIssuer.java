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
      BigDecimal manualMonthlyOperatingFee);
}
