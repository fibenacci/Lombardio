package io.lombardio.auction.application.service;

import java.math.BigDecimal;

public record PlaceBidCommand(
        String bidderDisplayName,
        BigDecimal amount
) {
}
