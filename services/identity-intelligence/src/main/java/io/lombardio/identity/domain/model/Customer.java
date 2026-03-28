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
  public String displayName() {
    return firstName + " " + lastName;
  }
}
