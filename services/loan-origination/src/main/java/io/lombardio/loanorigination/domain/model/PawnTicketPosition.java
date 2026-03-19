package io.lombardio.loanorigination.domain.model;

import java.math.BigDecimal;

public record PawnTicketPosition(
        String itemNumber,
        String itemBarcode,
        String label,
        String description,
        BigDecimal pledgedValue
) {
}
