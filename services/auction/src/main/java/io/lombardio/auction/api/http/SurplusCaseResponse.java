package io.lombardio.auction.api.http;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SurplusCaseResponse(
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
