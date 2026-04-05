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
package io.lombardio.loanorigination.infrastructure.persistence.entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "loan_positions")
public class LoanPositionEntity {

  @Id private String id;

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

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP",
      justification = "JPA relationship references must expose the managed entity association")
  public LoanCaseEntity getLoanCase() {
    return loanCase;
  }

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification =
          "JPA relationship references must store the managed entity association directly")
  public void setLoanCase(@NotNull LoanCaseEntity loanCase) {
    this.loanCase = Objects.requireNonNull(loanCase, "loanCase");
  }

  public Integer getTicketGroup() {
    return ticketGroup;
  }

  public void setTicketGroup(@NotNull Integer ticketGroup) {
    this.ticketGroup = Objects.requireNonNull(ticketGroup, "ticketGroup");
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(@NotNull String label) {
    this.label = Objects.requireNonNull(label, "label");
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(@NotNull String description) {
    this.description = Objects.requireNonNull(description, "description");
  }

  public String getGuidelineId() {
    return guidelineId;
  }

  public void setGuidelineId(@NotNull String guidelineId) {
    this.guidelineId = Objects.requireNonNull(guidelineId, "guidelineId");
  }

  public String getGuidelineLabel() {
    return guidelineLabel;
  }

  public void setGuidelineLabel(@NotNull String guidelineLabel) {
    this.guidelineLabel = Objects.requireNonNull(guidelineLabel, "guidelineLabel");
  }

  public BigDecimal getBaseLoanValue() {
    return baseLoanValue;
  }

  public void setBaseLoanValue(@NotNull BigDecimal baseLoanValue) {
    this.baseLoanValue = Objects.requireNonNull(baseLoanValue, "baseLoanValue");
  }

  public BigDecimal getPledgedValue() {
    return pledgedValue;
  }

  public void setPledgedValue(@NotNull BigDecimal pledgedValue) {
    this.pledgedValue = Objects.requireNonNull(pledgedValue, "pledgedValue");
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(@NotNull Integer sortOrder) {
    this.sortOrder = Objects.requireNonNull(sortOrder, "sortOrder");
  }
}
