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
package io.lombardio.loanorigination.domain.model;

import io.lombardio.loanorigination.application.service.CreateLoanPositionCommand;
import java.math.BigDecimal;
import java.util.UUID;

public record LoanPosition(
    String id,
    Integer ticketGroup,
    String label,
    String description,
    String guidelineId,
    String guidelineLabel,
    BigDecimal baseLoanValue,
    BigDecimal pledgedValue) {

  public static LoanPosition create(
      CreateLoanPositionCommand request, ValuationGuideline guideline) {
    BigDecimal pledgedValue =
        request.pledgedValue() != null ? request.pledgedValue() : guideline.baseLoanValue();

    return new LoanPosition(
        "position-" + UUID.randomUUID(),
        request.ticketGroup(),
        request.label(),
        request.description(),
        guideline.id(),
        guideline.label(),
        guideline.baseLoanValue(),
        pledgedValue);
  }
}
