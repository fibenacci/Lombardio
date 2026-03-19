package io.lombardio.auction.application.service;

import java.math.BigDecimal;

public record CreateAuctionLotCommand(
        String contractNumber,
        String itemNumber,
        String description,
        BigDecimal estimatedValue,
        BigDecimal outstandingClaim
) {
}
