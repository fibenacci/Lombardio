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
package io.lombardio.auction.infrastructure.persistence.adapter;

import io.lombardio.auction.domain.model.Auction;
import io.lombardio.auction.domain.port.AuctionRepository;
import io.lombardio.auction.infrastructure.persistence.mapper.PersistenceMapper;
import io.lombardio.auction.infrastructure.persistence.repository.SpringDataAuctionRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionPersistenceAdapter implements AuctionRepository {

  private final SpringDataAuctionRepository auctionRepository;
  private final PersistenceMapper mapper;

  @Override
  public List<Auction> findByTenantId(String tenantId) {
    return auctionRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public Optional<Auction> findByTenantIdAndId(String tenantId, String auctionId) {
    return auctionRepository.findByTenantIdAndId(tenantId, auctionId).map(mapper::toDomain);
  }

  @Override
  public Auction save(Auction auction) {
    return mapper.toDomain(auctionRepository.save(mapper.toEntity(auction)));
  }
}
