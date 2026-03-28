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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "pawn_ticket_positions", schema = "pawn_ticket")
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

  public PawnTicketEntity getPawnTicket() {
    return pawnTicket;
  }

  public void setPawnTicket(PawnTicketEntity pawnTicket) {
    this.pawnTicket = pawnTicket;
  }

  public String getItemNumber() {
    return itemNumber;
  }

  public void setItemNumber(String itemNumber) {
    this.itemNumber = itemNumber;
  }

  public String getItemBarcode() {
    return itemBarcode;
  }

  public void setItemBarcode(String itemBarcode) {
    this.itemBarcode = itemBarcode;
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

  public BigDecimal getPledgedValue() {
    return pledgedValue;
  }

  public void setPledgedValue(BigDecimal pledgedValue) {
    this.pledgedValue = pledgedValue;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }
}
