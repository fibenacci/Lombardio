package io.lombardio.onlineauction.api;

import java.math.BigDecimal;

public record OnlineAuctionLotResponse(
        String id,
        int lotNumber,
        String title,
        String description,
        BigDecimal startingBid,
        BigDecimal currentBid,
        String highestBidderAlias
) {
}
