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
package io.lombardio.loanorigination.api.http;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public record CreateLoanRequest(
    @NotBlank String customerId,
    @NotEmpty List<@Valid PositionPayload> positions,
    Integer termMonths,
    @DecimalMin("0.00") BigDecimal manualMonthlyOperatingFee,
    boolean thirdPartyPledgorPresentation,
    String bearerName,
    String bearerStreet,
    String bearerPostalCode,
    String bearerCity,
    String powerOfAttorneyDocumentDataUrl) {
  @AssertTrue(
      message =
          "powerOfAttorneyDocumentDataUrl must be provided when thirdPartyPledgorPresentation is true")
  public boolean hasPowerOfAttorneyWhenRequired() {
    return !thirdPartyPledgorPresentation
        || (powerOfAttorneyDocumentDataUrl != null && !powerOfAttorneyDocumentDataUrl.isBlank());
  }

  @AssertTrue(message = "bearerName must be provided when thirdPartyPledgorPresentation is true")
  public boolean hasBearerNameWhenRequired() {
    return !thirdPartyPledgorPresentation || (bearerName != null && !bearerName.isBlank());
  }
}
