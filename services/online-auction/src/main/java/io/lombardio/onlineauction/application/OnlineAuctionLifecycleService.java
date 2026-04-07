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
package io.lombardio.onlineauction.application;

import io.lombardio.onlineauction.api.CreateOnlineAuctionRequest;
import io.lombardio.onlineauction.api.OnlineAuctionNotFoundException;
import io.lombardio.onlineauction.api.OnlineAuctionResponse;
import io.lombardio.onlineauction.domain.OnlineAuction;
import io.lombardio.onlineauction.domain.OnlineAuctionRepository;
import io.lombardio.onlineauction.domain.RealtimePublisher;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OnlineAuctionLifecycleService {

  private final OnlineAuctionRepository auctionRepository;
  private final RealtimePublisher realtimePublisher;
  private final OnlineAuctionMetrics metrics;
  private final OnlineAuctionMapper mapper;

  public OnlineAuctionLifecycleService(
      OnlineAuctionRepository auctionRepository,
      RealtimePublisher realtimePublisher,
      OnlineAuctionMetrics metrics,
      OnlineAuctionMapper mapper) {
    this.auctionRepository = auctionRepository;
    this.realtimePublisher = realtimePublisher;
    this.metrics = metrics;
    this.mapper = mapper;
  }

  public List<OnlineAuctionResponse> listAdminAuctions(String tenantId) {
    return auctionRepository.findByTenantId(tenantId).stream()
        .sorted(Comparator.comparing(OnlineAuction::createdAt).reversed())
        .map(mapper::toAdminResponse)
        .toList();
  }

  public OnlineAuctionResponse createAuction(String tenantId, CreateOnlineAuctionRequest request) {
    OnlineAuction saved =
        auctionRepository.save(OnlineAuction.createDraft(tenantId, request, Instant.now()));
    metrics.recordAuctionCreated();
    return mapper.toAdminResponse(saved);
  }

  public OnlineAuctionResponse publishAuction(String tenantId, String auctionId) {
    OnlineAuction current = requireAuction(tenantId, auctionId);
    OnlineAuction updated = current.publish(Instant.now());
    OnlineAuction saved = auctionRepository.save(updated);
    publishEvent(saved.channelName(), "auction_published", saved);
    return mapper.toAdminResponse(saved);
  }

  public OnlineAuctionResponse startAuction(String tenantId, String auctionId) {
    OnlineAuction current = requireAuction(tenantId, auctionId);
    OnlineAuction updated = current.start(Instant.now());
    OnlineAuction saved = auctionRepository.save(updated);
    publishEvent(saved.channelName(), "auction_live", saved);
    return mapper.toAdminResponse(saved);
  }

  public OnlineAuctionResponse closeAuction(String tenantId, String auctionId) {
    OnlineAuction current = requireAuction(tenantId, auctionId);
    OnlineAuction updated = current.close(Instant.now());
    OnlineAuction saved = auctionRepository.save(updated);
    publishEvent(saved.channelName(), "auction_closed", saved);
    return mapper.toAdminResponse(saved);
  }

  public List<OnlineAuctionResponse> listPublicAuctions(String tenantId) {
    return auctionRepository.findPublicByTenantId(tenantId).stream()
        .map(mapper::toPublicResponse)
        .toList();
  }

  public OnlineAuctionResponse getPublicAuction(String tenantId, String auctionId) {
    return mapper.toPublicResponse(requirePublicAuction(tenantId, auctionId));
  }

  public OnlineAuction requireAuction(String tenantId, String auctionId) {
    return auctionRepository
        .findByTenantIdAndId(tenantId, auctionId)
        .orElseThrow(() -> new OnlineAuctionNotFoundException("Online auction not found"));
  }

  public OnlineAuction requirePublicAuction(String tenantId, String auctionId) {
    return auctionRepository
        .findPublicByTenantIdAndId(tenantId, auctionId)
        .orElseThrow(() -> new OnlineAuctionNotFoundException("Public online auction not found"));
  }

  private void publishEvent(String channel, String eventType, Object payload) {
    realtimePublisher.publish(channel, Map.of("type", eventType, "payload", payload));
  }
}
