package io.lombardio.kyc.infrastructure.persistence;

import io.lombardio.kyc.domain.KycStatus;
import io.lombardio.kyc.domain.KycVerificationMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "kyc_records", schema = "kyc")
public class KycRecordEntity {

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_mode", nullable = false)
    private KycVerificationMode verificationMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private KycStatus status;

    @Column(name = "verified_until")
    private LocalDate verifiedUntil;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "document_valid_until")
    private LocalDate documentValidUntil;

    @Column(name = "document_front_image_data_url", columnDefinition = "text")
    private String documentFrontImageDataUrl;

    @Column(name = "document_back_image_data_url", columnDefinition = "text")
    private String documentBackImageDataUrl;

    @Column(name = "decision_note")
    private String decisionNote;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "provider_reference")
    private String providerReference;

    @Column(name = "provider_status")
    private String providerStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public KycVerificationMode getVerificationMode() {
        return verificationMode;
    }

    public void setVerificationMode(KycVerificationMode verificationMode) {
        this.verificationMode = verificationMode;
    }

    public KycStatus getStatus() {
        return status;
    }

    public void setStatus(KycStatus status) {
        this.status = status;
    }

    public LocalDate getVerifiedUntil() {
        return verifiedUntil;
    }

    public void setVerifiedUntil(LocalDate verifiedUntil) {
        this.verifiedUntil = verifiedUntil;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public LocalDate getDocumentValidUntil() {
        return documentValidUntil;
    }

    public void setDocumentValidUntil(LocalDate documentValidUntil) {
        this.documentValidUntil = documentValidUntil;
    }

    public String getDocumentFrontImageDataUrl() {
        return documentFrontImageDataUrl;
    }

    public void setDocumentFrontImageDataUrl(String documentFrontImageDataUrl) {
        this.documentFrontImageDataUrl = documentFrontImageDataUrl;
    }

    public String getDocumentBackImageDataUrl() {
        return documentBackImageDataUrl;
    }

    public void setDocumentBackImageDataUrl(String documentBackImageDataUrl) {
        this.documentBackImageDataUrl = documentBackImageDataUrl;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public void setDecisionNote(String decisionNote) {
        this.decisionNote = decisionNote;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }

    public String getProviderStatus() {
        return providerStatus;
    }

    public void setProviderStatus(String providerStatus) {
        this.providerStatus = providerStatus;
    }
}
