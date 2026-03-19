import { reactive } from "vue";
import { authStore } from "./auth";
import { fetchTenantFeatures, fetchTenants } from "../services/api/platform";

export const tenantStore = reactive({
  tenants: [],
  selectedTenantId: "",
  features: [],
  async initialize() {
    await this.refreshTenants();
  },
  
  async refreshTenants() {
    if (!authStore.user) {
      this.tenants = [];
      this.selectedTenantId = "";
      this.features = [];
      return;
    }

    if (!authStore.canManagePlatform()) {
      this.tenants = [
        {
          id: authStore.user.tenantId,
          key: authStore.user.tenantId,
          displayName: authStore.user.tenantId,
          status: "ACTIVE"
        }
      ];
      this.selectedTenantId = authStore.user.tenantId;
      await this.refreshFeatures();
      return;
    }

    const tenants = await fetchTenants(authStore.token);
    this.tenants = tenants;

    if (!this.selectedTenantId && tenants.length > 0) {
      this.selectedTenantId = tenants[0].id;
    }

    if (this.selectedTenantId) {
      await this.refreshFeatures();
    } else {
      this.features = [];
    }
  },

  async selectTenant(tenantId) {
    this.selectedTenantId = tenantId;
    await this.refreshFeatures();
  },

  async refreshFeatures() {
    if (!this.selectedTenantId) {
      this.features = [];
      return;
    }

    this.features = await fetchTenantFeatures(this.selectedTenantId, authStore.token);
  },

  selectedTenant() {
    return this.tenants.find((tenant) => tenant.id === this.selectedTenantId) ?? null;
  },

  hasFeature(featureKey) {
    return this.features.some((feature) => feature.featureKey === featureKey && feature.enabled);
  },

  resetForTests() {
    this.tenants = [];
    this.selectedTenantId = "";
    this.features = [];
  }
});
