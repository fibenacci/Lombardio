package io.lombardio.auction.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SurplusCase(
        String auctionId,
        String lotId,
        int lotNumber,
        String contractNumber,
        BigDecimal hammerPrice,
        BigDecimal outstandingClaim,
        BigDecimal surplusAmount,
        LocalDate authorityTransferDueDate,
        String authorityTransferStatus
) {
}
