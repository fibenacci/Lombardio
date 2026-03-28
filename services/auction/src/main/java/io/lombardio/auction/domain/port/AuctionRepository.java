/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */
package io.lombardio.auction.domain.port;

import io.lombardio.auction.domain.model.Auction;
import java.util.List;
import java.util.Optional;

public interface AuctionRepository {

  List<Auction> findByTenantId(String tenantId);

  Optional<Auction> findByTenantIdAndId(String tenantId, String auctionId);

  Auction save(Auction auction);
}
