package io.lombardio.identity.api.http;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateCustomerRequest(
        @NotBlank String customerNumber,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull LocalDate birthDate,
        @NotBlank String phone,
        @Email String email,
        boolean wantsDigitalPawnTicket,
        String street,
        String postalCode,
        String city
) {
}
