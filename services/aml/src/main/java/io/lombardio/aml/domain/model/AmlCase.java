package io.lombardio.aml.domain.model;

import java.time.Instant;

public record AmlCase(
        String id,
        String tenantId,
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
        Instant updatedAt
) {
}
