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
import java.time.Instant;

public record AmlStatusView(
    String customerId,
    AmlStatus status,
    AmlRiskLevel riskLevel,
    boolean pepFlag,
    boolean sanctionsHit,
    boolean unusualTransactionFlag,
    boolean sourceOfFundsChecked,
    boolean suspiciousActivityReported,
    String goamlReference,
    String decisionNote,
    Instant lastScreenedAt,
    Instant reviewedAt,
    boolean featureAvailable,
    boolean originationAllowed,
    String decisionReason) {}
