package io.lombardio.onlineauction.infrastructure.persistence;

import io.lombardio.onlineauction.domain.OnlineAuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataOnlineAuctionRepository extends JpaRepository<OnlineAuctionEntity, String> {
    List<OnlineAuctionEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    List<OnlineAuctionEntity> findByTenantIdAndStatusInOrderByPublishedAtDesc(String tenantId, List<OnlineAuctionStatus> statuses);
    Optional<OnlineAuctionEntity> findByTenantIdAndId(String tenantId, String id);
    Optional<OnlineAuctionEntity> findByTenantIdAndIdAndStatusIn(String tenantId, String id, List<OnlineAuctionStatus> statuses);
}
