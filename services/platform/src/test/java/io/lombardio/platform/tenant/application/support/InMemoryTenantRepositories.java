package io.lombardio.platform.tenant.application.support;

import io.lombardio.platform.tenant.domain.Tenant;
import io.lombardio.platform.tenant.domain.TenantFeature;
import io.lombardio.platform.tenant.domain.TenantFeatureRepository;
import io.lombardio.platform.tenant.domain.TenantRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryTenantRepositories {

    private InMemoryTenantRepositories() {
    }

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
}
