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
package io.lombardio.identity.application.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(
    requiredProperties = {
      "id",
      "customerNumber",
      "firstName",
      "lastName",
      "birthDate",
      "displayName",
      "phone",
      "email",
      "wantsDigitalPawnTicket",
      "onlineAccessStatus",
      "kycStatus",
      "kycApproved",
      "kycDocumentType",
      "street",
      "postalCode",
      "city"
    })
public record CustomerView(
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String id,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String customerNumber,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String firstName,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String lastName,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate birthDate,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String displayName,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String phone,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String email,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean wantsDigitalPawnTicket,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String onlineAccessStatus,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String kycStatus,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean kycApproved,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String kycDocumentType,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String street,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String postalCode,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String city) {}
