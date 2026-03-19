package io.lombardio.pawnticket.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pawn_tickets", schema = "pawn_ticket")
public class PawnTicketEntity {

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

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "contract_number", nullable = false)
    private String contractNumber;

    @Column(name = "contract_barcode", nullable = false)
    private String contractBarcode;

    @Column(name = "ticket_number", nullable = false)
    private String ticketNumber;

    @Column(name = "terms_version", nullable = false)
    private String termsVersion;

    @Column(name = "terms_and_conditions_text", nullable = false, columnDefinition = "text")
    private String termsAndConditionsText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "earliest_auction_date", nullable = false)
    private LocalDate earliestAuctionDate;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "loan_amount", nullable = false)
    private BigDecimal loanAmount;

    @Column(name = "monthly_interest_rate", nullable = false)
    private BigDecimal monthlyInterestRate;

    @Column(name = "monthly_operating_fee", nullable = false)
    private BigDecimal monthlyOperatingFee;

    @Column(name = "manual_monthly_operating_fee_required", nullable = false)
    private boolean manualMonthlyOperatingFeeRequired;

    @Column(name = "total_interest_amount", nullable = false)
    private BigDecimal totalInterestAmount;

    @Column(name = "total_operating_fee_amount", nullable = false)
    private BigDecimal totalOperatingFeeAmount;

    @Column(name = "total_repayment_amount", nullable = false)
    private BigDecimal totalRepaymentAmount;

    @Column(name = "legal_text", nullable = false, columnDefinition = "text")
    private String legalText;

    @OneToMany(mappedBy = "pawnTicket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    private List<PawnTicketPositionEntity> positions = new ArrayList<>();

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

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getContractBarcode() {
        return contractBarcode;
    }

    public void setContractBarcode(String contractBarcode) {
        this.contractBarcode = contractBarcode;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getTermsVersion() {
        return termsVersion;
    }

    public void setTermsVersion(String termsVersion) {
        this.termsVersion = termsVersion;
    }

    public String getTermsAndConditionsText() {
        return termsAndConditionsText;
    }

    public void setTermsAndConditionsText(String termsAndConditionsText) {
        this.termsAndConditionsText = termsAndConditionsText;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getEarliestAuctionDate() {
        return earliestAuctionDate;
    }

    public void setEarliestAuctionDate(LocalDate earliestAuctionDate) {
        this.earliestAuctionDate = earliestAuctionDate;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public BigDecimal getMonthlyInterestRate() {
        return monthlyInterestRate;
    }

    public void setMonthlyInterestRate(BigDecimal monthlyInterestRate) {
        this.monthlyInterestRate = monthlyInterestRate;
    }

    public BigDecimal getMonthlyOperatingFee() {
        return monthlyOperatingFee;
    }

    public void setMonthlyOperatingFee(BigDecimal monthlyOperatingFee) {
        this.monthlyOperatingFee = monthlyOperatingFee;
    }

    public boolean isManualMonthlyOperatingFeeRequired() {
        return manualMonthlyOperatingFeeRequired;
    }

    public void setManualMonthlyOperatingFeeRequired(boolean manualMonthlyOperatingFeeRequired) {
        this.manualMonthlyOperatingFeeRequired = manualMonthlyOperatingFeeRequired;
    }

    public BigDecimal getTotalInterestAmount() {
        return totalInterestAmount;
    }

    public void setTotalInterestAmount(BigDecimal totalInterestAmount) {
        this.totalInterestAmount = totalInterestAmount;
    }

    public BigDecimal getTotalOperatingFeeAmount() {
        return totalOperatingFeeAmount;
    }

    public void setTotalOperatingFeeAmount(BigDecimal totalOperatingFeeAmount) {
        this.totalOperatingFeeAmount = totalOperatingFeeAmount;
    }

    public BigDecimal getTotalRepaymentAmount() {
        return totalRepaymentAmount;
    }

    public void setTotalRepaymentAmount(BigDecimal totalRepaymentAmount) {
        this.totalRepaymentAmount = totalRepaymentAmount;
    }

    public String getLegalText() {
        return legalText;
    }

    public void setLegalText(String legalText) {
        this.legalText = legalText;
    }

    public List<PawnTicketPositionEntity> getPositions() {
        return positions;
    }

    public void setPositions(List<PawnTicketPositionEntity> positions) {
        this.positions = positions;
    }
}
