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
package io.lombardio.pawnticket.infrastructure.persistence.entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "pawn_ticket_positions")
public class PawnTicketPositionEntity {

  @Id private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pawn_ticket_id", nullable = false)
  private PawnTicketEntity pawnTicket;

  @Column(name = "item_number", nullable = false)
  private String itemNumber;

  @Column(name = "item_barcode", nullable = false)
  private String itemBarcode;

  @Column(name = "label", nullable = false)
  private String label;

  @Column(name = "description", nullable = false)
  private String description;

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
  public PawnTicketEntity getPawnTicket() {
    return pawnTicket;
  }

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification =
          "JPA relationship references must store the managed entity association directly")
  public void setPawnTicket(@NotNull PawnTicketEntity pawnTicket) {
    this.pawnTicket = Objects.requireNonNull(pawnTicket, "pawnTicket");
  }

  public String getItemNumber() {
    return itemNumber;
  }

  public void setItemNumber(@NotNull String itemNumber) {
    this.itemNumber = Objects.requireNonNull(itemNumber, "itemNumber");
  }

  public String getItemBarcode() {
    return itemBarcode;
  }

  public void setItemBarcode(@NotNull String itemBarcode) {
    this.itemBarcode = Objects.requireNonNull(itemBarcode, "itemBarcode");
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
