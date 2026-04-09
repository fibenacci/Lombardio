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
package io.lombardio.pawnticket.api.http;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(
    requiredProperties = {
      "itemNumber",
      "itemBarcode",
      "label",
      "description",
      "pledgedValue"
    })
public record PawnTicketPositionResponse(
    @NotNull String itemNumber,
    @NotNull String itemBarcode,
    @NotNull String label,
    @NotNull String description,
    @NotNull BigDecimal pledgedValue) {}
