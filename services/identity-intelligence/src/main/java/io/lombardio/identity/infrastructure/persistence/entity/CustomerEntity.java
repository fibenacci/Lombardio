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
package io.lombardio.identity.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "customers")
public class CustomerEntity {

  @Id private String id;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "customer_number", nullable = false)
  private String customerNumber;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "phone", nullable = false)
  private String phone;

  @Column(name = "email")
  private String email;

  @Column(name = "wants_digital_pawn_ticket", nullable = false)
  private boolean wantsDigitalPawnTicket;

  @Column(name = "online_access_status", nullable = false)
  private String onlineAccessStatus;

  @Column(name = "street")
  private String street;

  @Column(name = "postal_code")
  private String postalCode;

  @Column(name = "city")
  private String city;

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

  public String getCustomerNumber() {
    return customerNumber;
  }

  public void setCustomerNumber(@NotNull String customerNumber) {
    this.customerNumber = Objects.requireNonNull(customerNumber, "customerNumber");
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(@NotNull String firstName) {
    this.firstName = Objects.requireNonNull(firstName, "firstName");
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(@NotNull String lastName) {
    this.lastName = Objects.requireNonNull(lastName, "lastName");
  }

  public String getPhone() {
    return phone;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(@NotNull LocalDate birthDate) {
    this.birthDate = Objects.requireNonNull(birthDate, "birthDate");
  }

  public void setPhone(@NotNull String phone) {
    this.phone = Objects.requireNonNull(phone, "phone");
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean isWantsDigitalPawnTicket() {
    return wantsDigitalPawnTicket;
  }

  public void setWantsDigitalPawnTicket(boolean wantsDigitalPawnTicket) {
    this.wantsDigitalPawnTicket = wantsDigitalPawnTicket;
  }

  public String getOnlineAccessStatus() {
    return onlineAccessStatus;
  }

  public void setOnlineAccessStatus(@NotNull String onlineAccessStatus) {
    this.onlineAccessStatus = Objects.requireNonNull(onlineAccessStatus, "onlineAccessStatus");
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }
}
