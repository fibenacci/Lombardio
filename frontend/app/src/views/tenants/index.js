import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { createTenant, upsertTenantFeature } from "../../services/api/platform";
import { useAppToast } from "../../composables/use-app-toast";
import { authStore } from "../../stores/auth";
import { tenantStore } from "../../stores/tenant";
import template from "./template.html?raw";
import "./styles.scss";

const FEATURE_CATALOG = [
  { key: "identity-access", label: "Identity Access" },
  { key: "customer-management", label: "Customer Management" },
  { key: "collateral-management", label: "Collateral Management" },
  { key: "aml-compliance", label: "AML Compliance" },
  { key: "kyc-provider-verification", label: "KYC Provider Verification" },
  { key: "kyc-document-ocr", label: "KYC Document OCR" }
];

export default defineComponent({
  name: "TenantsView",
  setup() {
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
    const selectedTenant = computed(() => tenantStore.selectedTenant());
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
      successMessage.value = "";
      errorMessage.value = "";

      try {
        const tenant = await createTenant({
          key: form.key,
          displayName: form.displayName,
          status: form.status
        }, authStore.token);

        await tenantStore.refreshTenants();
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
