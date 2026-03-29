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
package io.lombardio.platform.tenant.application.support;

import io.lombardio.platform.integration.domain.IntegrationOutboxEvent;
import io.lombardio.platform.integration.domain.IntegrationOutboxEventRepository;
import io.lombardio.platform.tenant.domain.Branch;
import io.lombardio.platform.tenant.domain.BranchRepository;
import io.lombardio.platform.tenant.domain.Tenant;
import io.lombardio.platform.tenant.domain.TenantFeature;
import io.lombardio.platform.tenant.domain.TenantFeatureRepository;
import io.lombardio.platform.tenant.domain.TenantRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryTenantRepositories {

  private InMemoryTenantRepositories() {}

  public static final class Tenants implements TenantRepository {
    private final Map<String, Tenant> store = new LinkedHashMap<>();

    @Override
    public List<Tenant> findAll() {
      return store.values().stream().toList();
    }

    @Override
    public Optional<Tenant> findById(String id) {
      return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Tenant> findByKey(String key) {
      return store.values().stream().filter(tenant -> tenant.key().equals(key)).findFirst();
    }

    @Override
    public Tenant save(Tenant tenant) {
      store.put(tenant.id(), tenant);
      return tenant;
    }
  }

  public static final class Features implements TenantFeatureRepository {
    private final Map<String, TenantFeature> store = new LinkedHashMap<>();

    @Override
    public List<TenantFeature> findByTenantId(String tenantId) {
      return store.values().stream()
          .filter(feature -> feature.tenantId().equals(tenantId))
          .toList();
    }

    @Override
    public Optional<TenantFeature> findByTenantIdAndFeatureKey(String tenantId, String featureKey) {
      return Optional.ofNullable(store.get(key(tenantId, featureKey)));
    }

    @Override
    public TenantFeature save(TenantFeature tenantFeature) {
      store.put(key(tenantFeature.tenantId(), tenantFeature.featureKey()), tenantFeature);
      return tenantFeature;
    }

    private String key(String tenantId, String featureKey) {
      return tenantId + ":" + featureKey;
    }
  }

  public static final class Branches implements BranchRepository {
    private final Map<String, Branch> store = new LinkedHashMap<>();

    @Override
    public List<Branch> findByTenantId(String tenantId) {
      return store.values().stream().filter(branch -> branch.tenantId().equals(tenantId)).toList();
    }

    @Override
    public Optional<Branch> findByTenantIdAndKey(String tenantId, String key) {
      return store.values().stream()
          .filter(branch -> branch.tenantId().equals(tenantId) && branch.key().equals(key))
          .findFirst();
    }

    @Override
    public Branch save(Branch branch) {
      store.put(branch.id(), branch);
      return branch;
    }
  }

  public static final class OutboxEvents implements IntegrationOutboxEventRepository {
    private final Map<String, IntegrationOutboxEvent> store = new LinkedHashMap<>();

    @Override
    public IntegrationOutboxEvent save(IntegrationOutboxEvent event) {
      store.put(event.id(), event);
      return event;
    }

    @Override
    public Optional<IntegrationOutboxEvent> findById(String id) {
      return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<IntegrationOutboxEvent> findClaimable(java.time.Instant now, int limit) {
      return store.values().stream()
          .filter(event -> event.nextAttemptAt().equals(now) || event.nextAttemptAt().isBefore(now))
          .filter(
              event ->
                  event.status()
                      == io.lombardio.platform.integration.domain.OutboxEventStatus.PENDING)
          .limit(limit)
          .toList();
    }

    public List<IntegrationOutboxEvent> findAll() {
      return store.values().stream().toList();
    }
  }
}
