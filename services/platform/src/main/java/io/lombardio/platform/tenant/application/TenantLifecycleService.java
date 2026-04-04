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
package io.lombardio.platform.tenant.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.lombardio.platform.iam.application.KeycloakService;
import io.lombardio.platform.integration.application.PlatformOutboxService;
import io.lombardio.platform.tenant.api.CreateTenantRequest;
import io.lombardio.platform.tenant.api.TenantResponse;
import io.lombardio.platform.tenant.api.UpdateTenantRequest;
import io.lombardio.platform.tenant.domain.Tenant;
import io.lombardio.platform.tenant.domain.TenantRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantLifecycleService {

  private final TenantRepository tenantRepository;
  private final PlatformOutboxService platformOutboxService;
  private final KeycloakService keycloakService;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed singleton bean")
  public TenantLifecycleService(
      TenantRepository tenantRepository,
      PlatformOutboxService platformOutboxService,
      KeycloakService keycloakService,
      ObjectMapper objectMapper,
      Clock clock) {
    this.tenantRepository = tenantRepository;
    this.platformOutboxService = platformOutboxService;
    this.keycloakService = keycloakService;
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  public List<TenantResponse> listTenants() {
    return tenantRepository.findAll().stream().map(this::toTenantResponse).toList();
  }

  @Transactional
  public TenantResponse createTenant(CreateTenantRequest request) {
    tenantRepository
        .findByKey(request.key())
        .ifPresent(
            tenant -> {
              throw new IllegalArgumentException("Tenant key already exists: " + request.key());
            });

    Instant now = Instant.now(clock);
    String tenantId = "tenant-" + UUID.randomUUID();
    Tenant tenant =
        new Tenant(tenantId, request.key(), request.displayName(), request.status(), now, now);

    // Create Keycloak group for tenant
    keycloakService.createTenantGroup(tenantId, request.displayName());

    Tenant saved = tenantRepository.save(tenant);
    recordTenantEvent("platform.tenant.created", saved);
    return toTenantResponse(saved);
  }

  @Transactional
  public TenantResponse updateTenant(String tenantId, UpdateTenantRequest request) {
    Tenant existing = requireTenant(tenantId);

    Tenant updated =
        new Tenant(
            existing.id(),
            request.key(),
            request.displayName(),
            request.status(),
            existing.createdAt(),
            Instant.now(clock));

    Tenant saved = tenantRepository.save(updated);
    recordTenantEvent("platform.tenant.updated", saved);
    return toTenantResponse(saved);
  }

  public Tenant requireTenant(String tenantId) {
    return tenantRepository
        .findById(tenantId)
        .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
  }

  private void recordTenantEvent(String eventType, Tenant tenant) {
    platformOutboxService.record(
        "tenant",
        tenant.id(),
        eventType,
        tenant.id(),
        toJson(
            Map.of(
                "tenantId",
                tenant.id(),
                "key",
                tenant.key(),
                "displayName",
                tenant.displayName(),
                "status",
                tenant.status(),
                "createdAt",
                tenant.createdAt().toString(),
                "updatedAt",
                tenant.updatedAt().toString())));
  }

  private String toJson(Object payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize outbox payload", exception);
    }
  }

  private TenantResponse toTenantResponse(Tenant tenant) {
    return new TenantResponse(
        tenant.id(),
        tenant.key(),
        tenant.displayName(),
        tenant.status(),
        tenant.createdAt(),
        tenant.updatedAt());
  }
}
