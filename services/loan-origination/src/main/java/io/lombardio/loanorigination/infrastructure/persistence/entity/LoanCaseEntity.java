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
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_cases")
@Getter
@Setter
@NoArgsConstructor
@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
    justification = "JPA entities")
public class LoanCaseEntity {

  @Id private String id;

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

  @OneToMany(
      mappedBy = "loanCase",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<PledgeRecordEntity> pledgeRecords = new ArrayList<>();

  @OneToMany(
      mappedBy = "loanCase",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<LoanPositionEntity> positions = new ArrayList<>();

  @OneToMany(
      mappedBy = "loanCase",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<LoanPawnTicketEntity> pawnTickets = new ArrayList<>();
}
