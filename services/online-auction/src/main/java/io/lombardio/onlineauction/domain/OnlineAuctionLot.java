package io.lombardio.onlineauction.domain;

import java.math.BigDecimal;

public record OnlineAuctionLot(
        String id,
        int lotNumber,
        String title,
        String description,
        BigDecimal startingBid,
        BigDecimal currentBid,
        String highestBidderAlias
) {
}
