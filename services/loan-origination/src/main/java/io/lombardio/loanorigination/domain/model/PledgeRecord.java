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

import io.lombardio.loanorigination.application.service.CreateLoanCommand;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PledgeRecord(
    String id,
    String loanCaseId,
    String tenantId,
    Instant recordedAt,
    String languageCode,
    LocalDate retentionUntil,
    String pledgorName,
    String pledgorStreet,
    String pledgorPostalCode,
    String pledgorCity,
    LocalDate pledgorBirthDate,
    String checkedDocumentType,
    boolean powerOfAttorneyRequired,
    String bearerName,
    String bearerStreet,
    String bearerPostalCode,
    String bearerCity,
    String powerOfAttorneyDocumentDataUrl) {

  public static PledgeRecord create(
      String loanCaseId,
      String tenantId,
      CustomerProfile customer,
      CreateLoanCommand request,
      Instant now) {

    if (request.thirdPartyPledgorPresentation()) {
      if (request.bearerName() == null || request.bearerName().isBlank()) {
        throw new IllegalArgumentException("bearerName is required for third-party presentation");
      }
      if (request.powerOfAttorneyDocumentDataUrl() == null
          || request.powerOfAttorneyDocumentDataUrl().isBlank()) {
        throw new IllegalArgumentException(
            "powerOfAttorneyDocumentDataUrl is required for third-party presentation");
      }
    }

    return new PledgeRecord(
        "pledge-" + UUID.randomUUID(),
        loanCaseId,
        tenantId,
        now,
        "de",
        now.atZone(java.time.ZoneOffset.UTC).toLocalDate().plusYears(4),
        customer.displayName(),
        customer.street(),
        customer.postalCode(),
        customer.city(),
        customer.birthDate(),
        customer.checkedDocumentType(),
        request.thirdPartyPledgorPresentation(),
        request.thirdPartyPledgorPresentation() ? request.bearerName() : null,
        request.thirdPartyPledgorPresentation() ? request.bearerStreet() : null,
        request.thirdPartyPledgorPresentation() ? request.bearerPostalCode() : null,
        request.thirdPartyPledgorPresentation() ? request.bearerCity() : null,
        request.thirdPartyPledgorPresentation() ? request.powerOfAttorneyDocumentDataUrl() : null);
  }
}
