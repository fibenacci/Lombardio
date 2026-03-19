package io.lombardio.pawnticket.infrastructure.persistence.entity;

import io.lombardio.pawnticket.domain.model.CashTransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cash_transactions", schema = "pawn_ticket")
public class CashTransactionEntity {

    @Id
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "ticket_number", nullable = false)
    private String ticketNumber;

    @Column(name = "customer_number", nullable = false)
    private String customerNumber;

    @Column(name = "customer_display_name", nullable = false)
    private String customerDisplayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CashTransactionType type;

    @Column(name = "outstanding_loan_amount", nullable = false)
    private BigDecimal outstandingLoanAmount;

    @Column(name = "interest_amount", nullable = false)
    private BigDecimal interestAmount;

    @Column(name = "operating_fee_amount", nullable = false)
    private BigDecimal operatingFeeAmount;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "legal_text", nullable = false, columnDefinition = "text")
    private String legalText;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

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

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
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

    public CashTransactionType getType() {
        return type;
    }

    public void setType(CashTransactionType type) {
        this.type = type;
    }

    public BigDecimal getOutstandingLoanAmount() {
        return outstandingLoanAmount;
    }

    public void setOutstandingLoanAmount(BigDecimal outstandingLoanAmount) {
        this.outstandingLoanAmount = outstandingLoanAmount;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
    }

    public BigDecimal getOperatingFeeAmount() {
        return operatingFeeAmount;
    }

    public void setOperatingFeeAmount(BigDecimal operatingFeeAmount) {
        this.operatingFeeAmount = operatingFeeAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getLegalText() {
        return legalText;
    }

    public void setLegalText(String legalText) {
        this.legalText = legalText;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
