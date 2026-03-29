import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { createTenant, upsertTenantFeature } from "../../services/api/platform";
import { useAppToast } from "../../composables/use-app-toast";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import template from "./template.html?raw";
import "./styles.scss";

const FEATURE_CATALOG = [
  { key: "identity-access", label: "Identity Access", description: "Internal identity and access management for the tenant's employees." },
  { key: "loan-origination", label: "Loan Origination", description: "Automated credit scoring and loan origination workflows." },
  { key: "aml-compliance", label: "AML Compliance", description: "Anti-money laundering and KYC verification." },
  { key: "online-auctions", label: "Online Auctions", description: "Digital auction platform for pawned items." },
  { key: "reporting", label: "Reporting", description: "Dashboard and business intelligence reports." }
];

export default defineComponent({
  name: "TenantsView",
  setup() {
    console.log("INDEX useAuthStore IDENTITY:", useAuthStore.toString().slice(0, 100));
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const toast = useAppToast();
    const isLoading = ref(true);
    const errorMessage = ref("");
    const successMessage = ref("");
    const form = reactive({
      key: "",
      displayName: "",
      status: "ACTIVE"
    });

    const tenants = computed(() => tenantStore.tenants);
    const selectedTenant = computed(() => tenantStore.selectedTenant);
    const tenantFeatures = computed(() =>
      FEATURE_CATALOG.map((catalogEntry) => ({
        ...catalogEntry,
        enabled:
          tenantStore.features.find((feature) => feature.featureKey === catalogEntry.key)?.enabled ?? false
      }))
    );

    function tenantStatusSeverity(status) {
      return status === "ACTIVE" ? "success" : "contrast";
    }

    async function loadData() {
      isLoading.value = true;
      errorMessage.value = "";

      try {
        await tenantStore.refreshTenants();
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : "Failed to load tenants";
      } finally {
        isLoading.value = false;
      }
    }

    async function submit() {
      console.log("SUBMITTING TENANT FORM", form);
      console.log("SUBMITTING WITH USER:", authStore.user?.id, "CAN MANAGE:", authStore.canManagePlatform);
      successMessage.value = "";
      errorMessage.value = "";

      try {
        const tenant = await createTenant({
          key: form.key,
          displayName: form.displayName,
          status: form.status
        }, authStore.token);
        console.log("TENANT CREATED BY API:", tenant);

        await tenantStore.refreshTenants();
        console.log("STORE REFRESHED, TENANTS:", tenantStore.tenants);
        await tenantStore.selectTenant(tenant.id);
        successMessage.value = "Tenant created";
        toast.success("Tenant created", `${tenant.displayName} is ready for configuration.`);
        form.key = "";
        form.displayName = "";
        form.status = "ACTIVE";
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : "Failed to create tenant";
      }
    }

    async function selectTenant(tenantId) {
      errorMessage.value = "";
      successMessage.value = "";

      try {
        await tenantStore.selectTenant(tenantId);
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : "Failed to load tenant features";
      }
    }

    async function toggleFeature(featureKey, enabled) {
      if (!tenantStore.selectedTenantId) {
        return;
      }

      successMessage.value = "";
      errorMessage.value = "";

      try {
        await upsertTenantFeature(tenantStore.selectedTenantId, featureKey, { enabled }, authStore.token);
        await tenantStore.refreshFeatures();
        successMessage.value = "Feature state updated";
        toast.success("Feature state updated", `${featureKey} is now ${enabled ? "enabled" : "disabled"}.`);
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : "Failed to update feature";
      }
    }

    onMounted(loadData);

    return {
      errorMessage,
      form,
      isLoading,
      selectTenant,
      selectedTenant,
      submit,
      successMessage,
      tenantStatusSeverity,
      tenantFeatures,
      tenantStore,
      tenants,
      toggleFeature
    };
  },
  template
});
