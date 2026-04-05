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
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "valuation_guidelines")
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

  public void setTenantId(@NotNull String tenantId) {
    this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(@NotNull String category) {
    this.category = Objects.requireNonNull(category, "category");
  }

  public String getMaterial() {
    return material;
  }

  public void setMaterial(@NotNull String material) {
    this.material = Objects.requireNonNull(material, "material");
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

  public BigDecimal getBaseLoanValue() {
    return baseLoanValue;
  }

  public void setBaseLoanValue(@NotNull BigDecimal baseLoanValue) {
    this.baseLoanValue = Objects.requireNonNull(baseLoanValue, "baseLoanValue");
  }
}
