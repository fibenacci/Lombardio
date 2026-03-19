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
