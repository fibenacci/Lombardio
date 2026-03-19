package io.lombardio.reporting.api.http;

import java.math.BigDecimal;

public record TransactionMixResponse(
        String type,
        Integer transactionCount,
        BigDecimal totalAmount
) {
}
