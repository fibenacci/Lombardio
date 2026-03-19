package io.lombardio.kyc.api;

import io.lombardio.kyc.application.KycService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/customers/{customerId}/kyc")
public class KycController {

    private final KycService kycService;
    private final io.lombardio.kyc.security.KycAuthorizationService authorizationService;

    public KycController(
            KycService kycService,
            io.lombardio.kyc.security.KycAuthorizationService authorizationService
    ) {
        this.kycService = kycService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public KycStatusResponse getStatus(
            @AuthenticationPrincipal io.lombardio.kyc.security.AuthenticatedKycUser principal,
            @PathVariable String tenantId,
            @PathVariable String customerId
    ) {
        authorizationService.requireRead(principal, tenantId);
        return kycService.getStatus(tenantId, customerId);
    }

    @PostMapping
    public KycStatusResponse updateStatus(
            @AuthenticationPrincipal io.lombardio.kyc.security.AuthenticatedKycUser principal,
            @PathVariable String tenantId,
            @PathVariable String customerId,
            @Valid @RequestBody UpdateKycStatusRequest request
    ) {
        authorizationService.requireWrite(principal, tenantId);
        return kycService.updateStatus(tenantId, customerId, request);
    }

    @PostMapping("/document-prefill")
    public DocumentPrefillResponse prefillDocument(
            @AuthenticationPrincipal io.lombardio.kyc.security.AuthenticatedKycUser principal,
            @PathVariable String tenantId,
            @PathVariable String customerId,
            @Valid @RequestBody DocumentPrefillRequest request
    ) {
        authorizationService.requireWrite(principal, tenantId);
        return kycService.prefillDocumentData(tenantId, request);
    }

    @GetMapping("/approval")
    public Map<String, Boolean> approval(
            @AuthenticationPrincipal io.lombardio.kyc.security.AuthenticatedKycUser principal,
            @PathVariable String tenantId,
            @PathVariable String customerId
    ) {
        authorizationService.requireRead(principal, tenantId);
        return Map.of("approved", kycService.isApproved(tenantId, customerId));
    }
}
