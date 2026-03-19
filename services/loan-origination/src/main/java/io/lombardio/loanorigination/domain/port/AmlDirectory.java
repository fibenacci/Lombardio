package io.lombardio.loanorigination.domain.port;

import java.math.BigDecimal;

public interface AmlDirectory {

    AmlAssessment assessForOrigination(String tenantId, String customerId, BigDecimal loanAmount);

    record AmlAssessment(
            boolean featureAvailable,
            boolean originationAllowed,
            String decisionReason
    ) {
    }
}
