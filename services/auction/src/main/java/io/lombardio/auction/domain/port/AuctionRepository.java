package io.lombardio.auction.domain.port;

import io.lombardio.auction.domain.model.Auction;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository {

    List<Auction> findByTenantId(String tenantId);

    Optional<Auction> findByTenantIdAndId(String tenantId, String auctionId);

    Auction save(Auction auction);
}
