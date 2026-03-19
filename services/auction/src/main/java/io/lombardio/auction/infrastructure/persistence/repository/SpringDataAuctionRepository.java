package io.lombardio.auction.infrastructure.persistence.repository;

import io.lombardio.auction.infrastructure.persistence.entity.AuctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataAuctionRepository extends JpaRepository<AuctionEntity, String> {

    List<AuctionEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    Optional<AuctionEntity> findByTenantIdAndId(String tenantId, String id);
}
