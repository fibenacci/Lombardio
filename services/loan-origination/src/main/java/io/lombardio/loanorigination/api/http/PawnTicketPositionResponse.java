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
package io.lombardio.loanorigination.api.http;

import java.math.BigDecimal;

public record PawnTicketPositionResponse(
    String itemNumber,
    String itemBarcode,
    String label,
    String description,
    BigDecimal pledgedValue) {}
