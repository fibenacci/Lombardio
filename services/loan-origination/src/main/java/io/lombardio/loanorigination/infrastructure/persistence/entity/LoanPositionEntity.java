package io.lombardio.loanorigination.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "loan_positions", schema = "loan_origination")
public class LoanPositionEntity {

    @Id
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_case_id", nullable = false)
    private LoanCaseEntity loanCase;

    @Column(name = "ticket_group", nullable = false)
    private Integer ticketGroup;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "guideline_id", nullable = false)
    private String guidelineId;

    @Column(name = "guideline_label", nullable = false)
    private String guidelineLabel;

    @Column(name = "base_loan_value", nullable = false)
    private BigDecimal baseLoanValue;

    @Column(name = "pledged_value", nullable = false)
    private BigDecimal pledgedValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LoanCaseEntity getLoanCase() {
        return loanCase;
    }

    public void setLoanCase(LoanCaseEntity loanCase) {
        this.loanCase = loanCase;
    }

    public Integer getTicketGroup() {
        return ticketGroup;
    }

    public void setTicketGroup(Integer ticketGroup) {
        this.ticketGroup = ticketGroup;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGuidelineId() {
        return guidelineId;
    }

    public void setGuidelineId(String guidelineId) {
        this.guidelineId = guidelineId;
    }

    public String getGuidelineLabel() {
        return guidelineLabel;
    }

    public void setGuidelineLabel(String guidelineLabel) {
        this.guidelineLabel = guidelineLabel;
    }

    public BigDecimal getBaseLoanValue() {
        return baseLoanValue;
    }

    public void setBaseLoanValue(BigDecimal baseLoanValue) {
        this.baseLoanValue = baseLoanValue;
    }

    public BigDecimal getPledgedValue() {
        return pledgedValue;
    }

    public void setPledgedValue(BigDecimal pledgedValue) {
        this.pledgedValue = pledgedValue;
    }
}
