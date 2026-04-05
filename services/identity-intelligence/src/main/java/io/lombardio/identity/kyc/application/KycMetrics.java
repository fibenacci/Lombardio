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
package io.lombardio.identity.kyc.application;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.identity.kyc.domain.KycStatus;
import io.lombardio.identity.kyc.domain.KycVerificationMode;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Locale;

public class KycMetrics {

  private final MeterRegistry meterRegistry;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification =
          "MeterRegistry is a shared instrumentation dependency and cannot be defensively copied")
  public KycMetrics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public static KycMetrics noop() {
    return new KycMetrics(new SimpleMeterRegistry());
  }

  public void recordStatusUpdate(KycStatus status, KycVerificationMode verificationMode) {
    meterRegistry
        .counter(
            "lombardio.kyc.status_updates",
            "status",
            status.name().toLowerCase(Locale.ROOT),
            "mode",
            verificationMode.name().toLowerCase(Locale.ROOT))
        .increment();
  }

  public void recordDocumentPrefill(boolean matched, String providerName) {
    meterRegistry
        .counter(
            "lombardio.kyc.document_prefill",
            "matched",
            Boolean.toString(matched),
            "provider",
            providerName == null || providerName.isBlank() ? "none" : providerName)
        .increment();
  }
}
