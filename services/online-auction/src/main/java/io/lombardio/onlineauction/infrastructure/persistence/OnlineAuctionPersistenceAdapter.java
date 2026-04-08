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

import io.lombardio.onlineauction.domain.OnlineAuction;
import io.lombardio.onlineauction.domain.OnlineAuctionRepository;
import io.lombardio.onlineauction.domain.OnlineAuctionStatus;
import io.lombardio.onlineauction.infrastructure.persistence.mapper.PersistenceMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class OnlineAuctionPersistenceAdapter implements OnlineAuctionRepository {

  private final SpringDataOnlineAuctionRepository repository;
  private final PersistenceMapper mapper;

  @Override
  @Transactional(readOnly = true)
  public List<OnlineAuction> findByTenantId(String tenantId) {
    return repository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<OnlineAuction> findPublicByTenantId(String tenantId) {
    return repository
        .findByTenantIdAndStatusInOrderByPublishedAtDesc(
            tenantId,
            List.of(
                OnlineAuctionStatus.PUBLISHED,
                OnlineAuctionStatus.LIVE,
                OnlineAuctionStatus.CLOSED))
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<OnlineAuction> findByTenantIdAndId(String tenantId, String auctionId) {
    return repository.findByTenantIdAndId(tenantId, auctionId).map(mapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<OnlineAuction> findPublicByTenantIdAndId(String tenantId, String auctionId) {
    return repository
        .findByTenantIdAndIdAndStatusIn(
            tenantId,
            auctionId,
            List.of(
                OnlineAuctionStatus.PUBLISHED,
                OnlineAuctionStatus.LIVE,
                OnlineAuctionStatus.CLOSED))
        .map(mapper::toDomain);
  }

  @Override
  public OnlineAuction save(OnlineAuction auction) {
    return mapper.toDomain(repository.save(mapper.toEntity(auction)));
  }
}
