package io.lombardio.auction.api.http;

import io.lombardio.auction.domain.model.AuctionLotStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AuctionLotResponse(
        String id,
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
