package io.lombardio.reporting.api.http;

import java.math.BigDecimal;

public record InventoryCategoryResponse(
        String category,
        Integer itemCount,
        BigDecimal pledgedValue
) {
}
