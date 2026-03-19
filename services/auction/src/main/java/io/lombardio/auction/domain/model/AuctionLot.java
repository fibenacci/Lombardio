package io.lombardio.auction.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AuctionLot(
        String id,
        String auctionId,
        int lotNumber,
        String contractNumber,
        String itemNumber,
        String description,
        BigDecimal estimatedValue,
        BigDecimal outstandingClaim,
        BigDecimal latestBidAmount,
        String leadingBidder,
        BigDecimal hammerPrice,
        AuctionLotStatus status,
        BigDecimal surplusAmount,
        LocalDate authorityTransferDueDate,
        String authorityTransferStatus
) {
}
