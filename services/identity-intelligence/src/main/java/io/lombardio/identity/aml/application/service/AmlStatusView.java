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
package io.lombardio.identity.aml.application.service;

import io.lombardio.identity.aml.domain.model.AmlRiskLevel;
import io.lombardio.identity.aml.domain.model.AmlStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Schema(
    requiredProperties = {
      "customerId",
      "status",
      "riskLevel",
      "pepFlag",
      "sanctionsHit",
      "unusualTransactionFlag",
      "sourceOfFundsChecked",
      "suspiciousActivityReported",
      "goamlReference",
      "decisionNote",
      "lastScreenedAt",
      "reviewedAt",
      "featureAvailable",
      "originationAllowed",
      "decisionReason"
    })
public record AmlStatusView(
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String customerId,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) AmlStatus status,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) AmlRiskLevel riskLevel,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean pepFlag,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean sanctionsHit,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean unusualTransactionFlag,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean sourceOfFundsChecked,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean suspiciousActivityReported,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String goamlReference,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String decisionNote,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant lastScreenedAt,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant reviewedAt,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean featureAvailable,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean originationAllowed,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String decisionReason) {}
