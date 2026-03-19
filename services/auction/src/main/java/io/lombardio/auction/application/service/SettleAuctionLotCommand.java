package io.lombardio.auction.application.service;

import java.math.BigDecimal;

public record SettleAuctionLotCommand(
        BigDecimal hammerPrice
) {
}
