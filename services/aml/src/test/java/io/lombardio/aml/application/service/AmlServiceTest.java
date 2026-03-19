package io.lombardio.aml.application.service;

import io.lombardio.aml.api.http.OriginationAssessmentRequest;
import io.lombardio.aml.api.http.UpdateAmlStatusRequest;
import io.lombardio.aml.domain.model.AmlRiskLevel;
import io.lombardio.aml.domain.model.AmlStatus;
import io.lombardio.aml.domain.port.TenantFeatureDirectory;
import io.lombardio.aml.infrastructure.persistence.support.InMemoryAmlRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmlServiceTest {

    private final InMemoryAmlRepository repository = new InMemoryAmlRepository();
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-18T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void shouldAllowOriginationWhenFeatureIsDisabled() {
        AmlService service = new AmlService(repository, (tenantId, featureKey) -> false, clock);

        var response = service.assessForOrigination("tenant-default", "customer-1", new OriginationAssessmentRequest(new BigDecimal("120.00")));

        assertFalse(response.featureAvailable());
        assertTrue(response.originationAllowed());
    }

    @Test
    void shouldBlockOriginationWhenCaseIsNotReviewed() {
        AmlService service = new AmlService(repository, enabledFeatures(Set.of(AmlService.AML_FEATURE_KEY)), clock);

        var response = service.assessForOrigination("tenant-default", "customer-1", new OriginationAssessmentRequest(new BigDecimal("120.00")));

        assertTrue(response.featureAvailable());
        assertFalse(response.originationAllowed());
        assertEquals(AmlStatus.NOT_REVIEWED, response.status());
    }

    @Test
    void shouldAllowOriginationForClearedCase() {
        AmlService service = new AmlService(repository, enabledFeatures(Set.of(AmlService.AML_FEATURE_KEY)), clock);
        service.updateStatus("tenant-default", "customer-1", new UpdateAmlStatusRequest(
                AmlStatus.CLEAR,
                AmlRiskLevel.LOW,
                false,
                false,
                false,
                true,
                false,
                null,
                "cleared",
                Instant.parse("2026-03-18T00:00:00Z"),
                Instant.parse("2026-03-18T00:05:00Z")
        ));

        var response = service.assessForOrigination("tenant-default", "customer-1", new OriginationAssessmentRequest(new BigDecimal("120.00")));

        assertTrue(response.originationAllowed());
        assertEquals(AmlStatus.CLEAR, response.status());
    }

    @Test
    void shouldRequireGoAmlReferenceWhenReported() {
        AmlService service = new AmlService(repository, enabledFeatures(Set.of(AmlService.AML_FEATURE_KEY)), clock);

        var exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                service.updateStatus("tenant-default", "customer-1", new UpdateAmlStatusRequest(
                        AmlStatus.REPORTED,
                        AmlRiskLevel.HIGH,
                        false,
                        false,
                        true,
                        true,
                        true,
                        null,
                        "reported",
                        Instant.parse("2026-03-18T00:00:00Z"),
                        Instant.parse("2026-03-18T00:05:00Z")
                ))
        );

        assertEquals("goamlReference is required when suspiciousActivityReported is true", exception.getMessage());
    }

    private TenantFeatureDirectory enabledFeatures(Set<String> features) {
        return (tenantId, featureKey) -> features.contains(featureKey);
    }
}
