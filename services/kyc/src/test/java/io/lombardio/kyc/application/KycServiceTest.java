package io.lombardio.kyc.application;

import io.lombardio.kyc.api.UpdateKycStatusRequest;
import io.lombardio.kyc.api.DocumentPrefillRequest;
import io.lombardio.kyc.domain.KycStatus;
import io.lombardio.kyc.domain.KycVerificationMode;
import io.lombardio.kyc.support.InMemoryKycRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KycServiceTest {

    private final KycService service = new KycService(
            new InMemoryKycRepository(),
            (tenantId, featureKey) -> "tenant-default".equals(tenantId)
                    && (KycService.PROVIDER_FEATURE_KEY.equals(featureKey) || KycService.OCR_FEATURE_KEY.equals(featureKey)),
            (tenantId, frontImageDataUrl, backImageDataUrl) -> java.util.Optional.of(
                    new io.lombardio.kyc.domain.DocumentOcrProvider.DocumentOcrResult(
                            "PERSONALAUSWEIS",
                            "XK1234567",
                            LocalDate.now().plusYears(5),
                            "test-ocr",
                            0.88d
                    )
            )
    );

    @Test
    void shouldReturnApprovedSeedStatus() {
        var status = service.getStatus("tenant-default", "customer-berlin-1");

        assertEquals(KycStatus.APPROVED, status.status());
        assertEquals(KycVerificationMode.MANUAL, status.verificationMode());
        assertTrue(status.providerVerificationAvailable());
        assertTrue(service.isApproved("tenant-default", "customer-berlin-1"));
    }

    @Test
    void shouldUpdateStatusForCustomer() {
        var updated = service.updateStatus(
                "tenant-default",
                "customer-new",
                new UpdateKycStatusRequest(
                        KycStatus.IN_PROGRESS,
                        KycVerificationMode.MANUAL,
                        LocalDate.now().plusMonths(1),
                        "REISEPASS",
                        "C01AB2345",
                        LocalDate.now().plusYears(5),
                        "data:image/png;base64,front",
                        "data:image/png;base64,back",
                        "Dokument aufgenommen",
                        null,
                        null,
                        null
                )
        );

        assertEquals(KycStatus.IN_PROGRESS, updated.status());
        assertEquals(KycVerificationMode.MANUAL, updated.verificationMode());
        assertFalse(service.isApproved("tenant-default", "customer-new"));
    }

    @Test
    void shouldAllowProviderVerificationWhenFeatureEnabled() {
        var updated = service.updateStatus(
                "tenant-default",
                "customer-provider",
                new UpdateKycStatusRequest(
                        KycStatus.APPROVED,
                        KycVerificationMode.PROVIDER,
                        LocalDate.now().plusYears(1),
                        "PERSONALAUSWEIS",
                        null,
                        null,
                        null,
                        null,
                        "Provider geprueft",
                        "veriff",
                        "provider-case-1",
                        "APPROVED"
                )
        );

        assertEquals(KycVerificationMode.PROVIDER, updated.verificationMode());
        assertEquals("veriff", updated.providerName());
        assertEquals("provider-case-1", updated.providerReference());
        assertEquals("APPROVED", updated.providerStatus());
    }

    @Test
    void shouldRejectProviderVerificationWhenFeatureDisabled() {
        KycService featureDisabledService = new KycService(
                new InMemoryKycRepository(),
                (tenantId, featureKey) -> false,
                (tenantId, frontImageDataUrl, backImageDataUrl) -> java.util.Optional.empty()
        );

        assertThrows(IllegalArgumentException.class, () -> featureDisabledService.updateStatus(
                "tenant-without-provider",
                "customer-provider",
                new UpdateKycStatusRequest(
                        KycStatus.IN_PROGRESS,
                        KycVerificationMode.PROVIDER,
                        LocalDate.now().plusDays(7),
                        "PERSONALAUSWEIS",
                        null,
                        null,
                        null,
                        null,
                        "Provider gestartet",
                        "veriff",
                        "provider-case-2",
                        "PENDING"
                )
        ));
    }

    @Test
    void shouldRejectManualApprovalWithoutIdentityDocuments() {
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(
                "tenant-default",
                "customer-manual",
                new UpdateKycStatusRequest(
                        KycStatus.APPROVED,
                        KycVerificationMode.MANUAL,
                        LocalDate.now().plusYears(1),
                        "PERSONALAUSWEIS",
                        "",
                        LocalDate.now().plusYears(5),
                        null,
                        null,
                        "Freigabe ohne Dokumente",
                        null,
                        null,
                        null
                )
        ));
    }

    @Test
    void shouldPrefillDocumentDataWhenOcrFeatureEnabled() {
        var prefill = service.prefillDocumentData(
                "tenant-default",
                new DocumentPrefillRequest("data:text/plain;base64,Zm9v", "data:text/plain;base64,YmFy")
        );

        assertTrue(prefill.available());
        assertTrue(prefill.matched());
        assertEquals("PERSONALAUSWEIS", prefill.documentType());
        assertEquals("XK1234567", prefill.documentNumber());
    }
}
