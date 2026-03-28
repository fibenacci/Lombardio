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

import java.time.LocalDate;

public record CustomerResponse(
    String id,
    String customerNumber,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String displayName,
    String phone,
    String email,
    boolean wantsDigitalPawnTicket,
    String onlineAccessStatus,
    String kycStatus,
    boolean kycApproved,
    String checkedDocumentType,
    String street,
    String postalCode,
    String city) {}
