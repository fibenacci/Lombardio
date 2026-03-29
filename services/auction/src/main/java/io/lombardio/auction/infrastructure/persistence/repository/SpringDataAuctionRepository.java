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
package io.lombardio.auction.infrastructure.persistence.repository;

import io.lombardio.auction.infrastructure.persistence.entity.AuctionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAuctionRepository extends JpaRepository<AuctionEntity, String> {

  @EntityGraph(attributePaths = "lots")
  List<AuctionEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);

  @EntityGraph(attributePaths = "lots")
  Optional<AuctionEntity> findByTenantIdAndId(String tenantId, String id);
}
