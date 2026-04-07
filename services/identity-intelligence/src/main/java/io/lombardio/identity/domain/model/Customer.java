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
package io.lombardio.identity.domain.model;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

public record Customer(
    String id,
    String tenantId,
    String customerNumber,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String phone,
    String email,
    boolean wantsDigitalPawnTicket,
    String onlineAccessStatus,
    String street,
    String postalCode,
    String city) {

  public Customer {
    email = normalizeEmail(email);
    if (wantsDigitalPawnTicket && (email == null || email.isBlank())) {
      throw new IllegalArgumentException(
          "Digital pawn ticket access requires a customer email address");
    }
  }

  public String displayName() {
    return firstName + " " + lastName;
  }

  public Customer update(
      String customerNumber,
      String firstName,
      String lastName,
      LocalDate birthDate,
      String phone,
      String email,
      boolean wantsDigitalPawnTicket,
      String street,
      String postalCode,
      String city) {
    String normalizedEmail = normalizeEmail(email);
    boolean emailChanged = !Objects.equals(this.email, normalizedEmail);
    String newStatus = determineOnlineAccessStatus(wantsDigitalPawnTicket, emailChanged);

    return new Customer(
        id,
        tenantId,
        customerNumber,
        firstName,
        lastName,
        birthDate,
        phone,
        normalizedEmail,
        wantsDigitalPawnTicket,
        newStatus,
        street,
        postalCode,
        city);
  }

  private String normalizeEmail(String email) {
    if (email == null) {
      return null;
    }
    String normalized = email.trim().toLowerCase(Locale.ROOT);
    return normalized.isEmpty() ? null : normalized;
  }

  private String determineOnlineAccessStatus(boolean wantsDigitalPawnTicket, boolean emailChanged) {
    if (!wantsDigitalPawnTicket) {
      return "NOT_REQUESTED";
    }
    if (emailChanged) {
      return "INVITED";
    }
    if (this.wantsDigitalPawnTicket
        && this.onlineAccessStatus != null
        && !this.onlineAccessStatus.isBlank()) {
      return this.onlineAccessStatus;
    }
    return "INVITED";
  }
}
