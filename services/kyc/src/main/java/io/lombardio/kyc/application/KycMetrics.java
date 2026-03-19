package io.lombardio.kyc.application;

import io.lombardio.kyc.domain.KycStatus;
import io.lombardio.kyc.domain.KycVerificationMode;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class KycMetrics {

    private final MeterRegistry meterRegistry;

    public KycMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public static KycMetrics noop() {
        return new KycMetrics(new SimpleMeterRegistry());
    }

    public void recordStatusUpdate(KycStatus status, KycVerificationMode verificationMode) {
        meterRegistry.counter(
                "lombardio.kyc.status_updates",
                "status",
                status.name().toLowerCase(),
                "mode",
                verificationMode.name().toLowerCase()
        ).increment();
    }

    public void recordDocumentPrefill(boolean matched, String providerName) {
        meterRegistry.counter(
                "lombardio.kyc.document_prefill",
                "matched",
                Boolean.toString(matched),
                "provider",
                providerName == null || providerName.isBlank() ? "none" : providerName
        ).increment();
    }
}
