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
package io.lombardio.identity.api.http;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateCustomerRequest(
    @NotBlank String customerNumber,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull LocalDate birthDate,
    @NotBlank String phone,
    @Email String email,
    boolean wantsDigitalPawnTicket,
    String street,
    String postalCode,
    String city) {}
