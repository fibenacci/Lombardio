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
package io.lombardio.identity.kyc.api;

import io.lombardio.identity.kyc.domain.KycStatus;
import io.lombardio.identity.kyc.domain.KycVerificationMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record KycStatusResponse(
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String customerId,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) KycVerificationMode verificationMode,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) KycStatus status,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate verifiedUntil,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String documentType,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String documentNumber,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate documentValidUntil,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String decisionNote,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String providerName,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String providerReference,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String providerStatus,
    @NotNull @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean providerVerificationAvailable) {}
