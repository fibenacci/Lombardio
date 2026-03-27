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
        String city
) {
    public String displayName() {
        return firstName + " " + lastName;
    }
}
