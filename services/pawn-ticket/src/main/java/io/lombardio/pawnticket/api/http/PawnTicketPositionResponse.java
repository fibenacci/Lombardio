package io.lombardio.pawnticket.api.http;

import java.math.BigDecimal;

public record PawnTicketPositionResponse(
        String itemNumber,
        String itemBarcode,
        String label,
        String description,
        BigDecimal pledgedValue
) {
}
