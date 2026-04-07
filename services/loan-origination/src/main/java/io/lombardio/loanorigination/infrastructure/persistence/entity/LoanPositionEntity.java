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
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_positions")
@Getter
@Setter
@NoArgsConstructor
@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
    justification = "JPA entities")
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
}
