import { defineStore } from "pinia";
import { useAuthStore } from "./auth";
import { fetchTenantFeatures, fetchTenants } from "../services/api/platform";

export const useTenantStore = defineStore("tenant", {
  state: () => ({
    tenants: [],
    selectedTenantId: "",
    features: []
  }),

  getters: {
    selectedTenant: (state) => state.tenants.find((tenant) => tenant.id === state.selectedTenantId) ?? null,
    hasFeature: (state) => (featureKey) => state.features.some((feature) => feature.featureKey === featureKey && feature.enabled)
  },

  actions: {
    async initialize() {
      await this.refreshTenants();
    },
    
    async refreshTenants() {
      const authStore = useAuthStore();
      if (!authStore.user) {
        this.tenants = [];
        this.selectedTenantId = "";
        this.features = [];
        return;
      }

      if (!authStore.canManagePlatform) {
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

      try {
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
      } catch (error) {
        console.error("Failed to fetch tenants", error);
        throw error;
      }
    },

    async selectTenant(tenantId) {
      this.selectedTenantId = tenantId;
      await this.refreshFeatures();
    },

    async refreshFeatures() {
      const authStore = useAuthStore();
      if (!this.selectedTenantId) {
        this.features = [];
        return;
      }

      if (!authStore.canManagePlatform && !authStore.user?.tenantId) {
        this.features = [];
        return;
      }

      try {
        this.features = await fetchTenantFeatures(this.selectedTenantId, authStore.token);
      } catch (error) {
        console.error("Failed to fetch tenant features", error);
        throw error;
      }
    },

    resetForTests() {
      this.tenants = [];
      this.selectedTenantId = "";
      this.features = [];
    }
  }
});
