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
package io.lombardio.onlineauction.domain;

import java.util.List;
import java.util.Optional;

public interface OnlineAuctionRepository {
  List<OnlineAuction> findByTenantId(String tenantId);

  List<OnlineAuction> findPublicByTenantId(String tenantId);

  Optional<OnlineAuction> findByTenantIdAndId(String tenantId, String auctionId);

  Optional<OnlineAuction> findPublicByTenantIdAndId(String tenantId, String auctionId);

  OnlineAuction save(OnlineAuction auction);
}
