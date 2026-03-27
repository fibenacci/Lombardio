package io.lombardio.identity.aml.api.http;

import io.lombardio.identity.aml.domain.model.AmlRiskLevel;
import io.lombardio.identity.aml.domain.model.AmlStatus;

import java.time.Instant;

public record AmlStatusResponse(
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
        String decisionReason
) {
}
