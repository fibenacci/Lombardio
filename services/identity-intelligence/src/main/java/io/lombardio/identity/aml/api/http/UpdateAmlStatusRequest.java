package io.lombardio.identity.aml.api.http;

import io.lombardio.identity.aml.domain.model.AmlRiskLevel;
import io.lombardio.identity.aml.domain.model.AmlStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateAmlStatusRequest(
        @NotNull AmlStatus status,
        @NotNull AmlRiskLevel riskLevel,
        boolean pepFlag,
        boolean sanctionsHit,
        boolean unusualTransactionFlag,
        boolean sourceOfFundsChecked,
        boolean suspiciousActivityReported,
        String goamlReference,
        String decisionNote,
        Instant lastScreenedAt,
        Instant reviewedAt
) {
}
