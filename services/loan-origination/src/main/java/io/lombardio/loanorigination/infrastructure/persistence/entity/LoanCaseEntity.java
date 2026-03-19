package io.lombardio.loanorigination.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loan_cases", schema = "loan_origination")
public class LoanCaseEntity {

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "customer_number", nullable = false)
    private String customerNumber;

    @Column(name = "customer_display_name", nullable = false)
    private String customerDisplayName;

    @Column(name = "customer_birth_date")
    private java.time.LocalDate customerBirthDate;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_street")
    private String customerStreet;

    @Column(name = "customer_postal_code")
    private String customerPostalCode;

    @Column(name = "customer_city")
    private String customerCity;

    @Column(name = "customer_kyc_status")
    private String customerKycStatus;

    @Column(name = "customer_kyc_approved", nullable = false)
    private boolean customerKycApproved;

    @Column(name = "customer_checked_document_type")
    private String customerCheckedDocumentType;

    @OneToMany(mappedBy = "loanCase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderColumn(name = "sort_order")
    private List<PledgeRecordEntity> pledgeRecords = new ArrayList<>();

    @OneToMany(mappedBy = "loanCase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderColumn(name = "sort_order")
    private List<LoanPositionEntity> positions = new ArrayList<>();

    @OneToMany(mappedBy = "loanCase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderColumn(name = "sort_order")
    private List<LoanPawnTicketEntity> pawnTickets = new ArrayList<>();

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

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getCustomerDisplayName() {
        return customerDisplayName;
    }

    public void setCustomerDisplayName(String customerDisplayName) {
        this.customerDisplayName = customerDisplayName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public java.time.LocalDate getCustomerBirthDate() {
        return customerBirthDate;
    }

    public void setCustomerBirthDate(java.time.LocalDate customerBirthDate) {
        this.customerBirthDate = customerBirthDate;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerStreet() {
        return customerStreet;
    }

    public void setCustomerStreet(String customerStreet) {
        this.customerStreet = customerStreet;
    }

    public String getCustomerPostalCode() {
        return customerPostalCode;
    }

    public void setCustomerPostalCode(String customerPostalCode) {
        this.customerPostalCode = customerPostalCode;
    }

    public String getCustomerCity() {
        return customerCity;
    }

    public void setCustomerCity(String customerCity) {
        this.customerCity = customerCity;
    }

    public String getCustomerKycStatus() {
        return customerKycStatus;
    }

    public void setCustomerKycStatus(String customerKycStatus) {
        this.customerKycStatus = customerKycStatus;
    }

    public boolean isCustomerKycApproved() {
        return customerKycApproved;
    }

    public void setCustomerKycApproved(boolean customerKycApproved) {
        this.customerKycApproved = customerKycApproved;
    }

    public String getCustomerCheckedDocumentType() {
        return customerCheckedDocumentType;
    }

    public void setCustomerCheckedDocumentType(String customerCheckedDocumentType) {
        this.customerCheckedDocumentType = customerCheckedDocumentType;
    }

    public List<PledgeRecordEntity> getPledgeRecords() {
        return pledgeRecords;
    }

    public void setPledgeRecords(List<PledgeRecordEntity> pledgeRecords) {
        this.pledgeRecords = pledgeRecords;
    }

    public List<LoanPositionEntity> getPositions() {
        return positions;
    }

    public void setPositions(List<LoanPositionEntity> positions) {
        this.positions = positions;
    }

    public List<LoanPawnTicketEntity> getPawnTickets() {
        return pawnTickets;
    }

    public void setPawnTickets(List<LoanPawnTicketEntity> pawnTickets) {
        this.pawnTickets = pawnTickets;
    }
}
