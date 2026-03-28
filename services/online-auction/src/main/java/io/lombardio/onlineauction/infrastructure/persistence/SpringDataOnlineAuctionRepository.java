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
package io.lombardio.onlineauction.infrastructure.persistence;

import io.lombardio.onlineauction.domain.OnlineAuctionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOnlineAuctionRepository
    extends JpaRepository<OnlineAuctionEntity, String> {
  List<OnlineAuctionEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);

  List<OnlineAuctionEntity> findByTenantIdAndStatusInOrderByPublishedAtDesc(
      String tenantId, List<OnlineAuctionStatus> statuses);

  Optional<OnlineAuctionEntity> findByTenantIdAndId(String tenantId, String id);

  Optional<OnlineAuctionEntity> findByTenantIdAndIdAndStatusIn(
      String tenantId, String id, List<OnlineAuctionStatus> statuses);
}
