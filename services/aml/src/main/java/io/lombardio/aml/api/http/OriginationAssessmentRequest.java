package io.lombardio.aml.api.http;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OriginationAssessmentRequest(
        @NotNull @DecimalMin("0.01") BigDecimal loanAmount
) {
}
