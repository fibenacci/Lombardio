package io.lombardio.aml.api.http;

import io.lombardio.aml.domain.model.AmlRiskLevel;
import io.lombardio.aml.domain.model.AmlStatus;
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
