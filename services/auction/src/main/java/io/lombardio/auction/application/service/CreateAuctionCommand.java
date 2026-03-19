package io.lombardio.auction.application.service;

import java.util.List;

public record CreateAuctionCommand(
        String title,
        String location,
        List<CreateAuctionLotCommand> lots
) {
}
