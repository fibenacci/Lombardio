package io.lombardio.loanorigination.api.http;

import java.math.BigDecimal;

public record PawnTicketPositionResponse(
        String itemNumber,
        String itemBarcode,
        String label,
        String description,
        BigDecimal pledgedValue
) {
}
