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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "valuation_guidelines", schema = "loan_origination")
public class ValuationGuidelineEntity {

  @Id private String id;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "category", nullable = false)
  private String category;

  @Column(name = "material", nullable = false)
  private String material;

  @Column(name = "label", nullable = false)
  private String label;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "base_loan_value", nullable = false)
  private BigDecimal baseLoanValue;

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

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getMaterial() {
    return material;
  }

  public void setMaterial(String material) {
    this.material = material;
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

  public BigDecimal getBaseLoanValue() {
    return baseLoanValue;
  }

  public void setBaseLoanValue(BigDecimal baseLoanValue) {
    this.baseLoanValue = baseLoanValue;
  }
}
