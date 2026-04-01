import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { useAppToast } from "../../../../../shared/ui/composables/use-app-toast";
import { useI18n } from "../../../../../app/i18n";
import { useAuthStore } from "../../../../../app/session/state/auth.store";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { createHttpTenantsAdapter } from "../../../infrastructure/adapters/http-tenants.adapter";
import template from "./template.html?raw";
import "./styles.scss";

const FEATURE_CATALOG = [
  { key: "identity-access" },
  { key: "loan-origination" },
  { key: "aml-compliance" },
  { key: "online-auctions" },
  { key: "reporting" }
];

export default defineComponent({
  name: "TenantsPage",
  setup() {
    const { t } = useI18n();
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const toast = useAppToast();
    const tenantsAdapter = createHttpTenantsAdapter();
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
    const statusOptions = computed(() => [
      { label: t("tenants.status.ACTIVE"), value: "ACTIVE" },
      { label: t("tenants.status.INACTIVE"), value: "INACTIVE" }
    ]);
    const tenantFeatures = computed(() =>
      FEATURE_CATALOG.map((catalogEntry) => ({
        ...catalogEntry,
        label: t(`tenants.features.catalog.${catalogEntry.key}.label`),
        description: t(`tenants.features.catalog.${catalogEntry.key}.description`),
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
        errorMessage.value = error instanceof Error ? error.message : t("tenants.messages.loadFailed");
      } finally {
        isLoading.value = false;
      }
    }

    async function submit() {
      successMessage.value = "";
      errorMessage.value = "";

      try {
        const tenant = await tenantsAdapter.createTenant(
          {
            key: form.key,
            displayName: form.displayName,
            status: form.status
          },
          authStore.token
        );

        await tenantStore.refreshTenants();
        await tenantStore.selectTenant(tenant.id);
        successMessage.value = t("tenants.messages.createdTitle");
        toast.success(t("tenants.messages.createdTitle"), t("tenants.messages.createdToast", { tenant: tenant.displayName }));
        form.key = "";
        form.displayName = "";
        form.status = "ACTIVE";
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("tenants.messages.createFailed");
      }
    }

    async function selectTenant(tenantId) {
      errorMessage.value = "";
      successMessage.value = "";

      try {
        await tenantStore.selectTenant(tenantId);
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("tenants.messages.featuresLoadFailed");
      }
    }

    async function toggleFeature(featureKey, enabled) {
      if (!tenantStore.selectedTenantId) {
        return;
      }

      successMessage.value = "";
      errorMessage.value = "";

      try {
        await tenantsAdapter.upsertTenantFeature(tenantStore.selectedTenantId, featureKey, { enabled }, authStore.token);
        await tenantStore.refreshFeatures();
        successMessage.value = t("tenants.messages.featureUpdatedTitle");
        toast.success(
          t("tenants.messages.featureUpdatedTitle"),
          t("tenants.messages.featureUpdatedToast", {
            feature: t(`tenants.features.catalog.${featureKey}.label`),
            state: enabled ? t("tenants.featureState.enabled") : t("tenants.featureState.disabled")
          })
        );
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("tenants.messages.featureUpdateFailed");
      }
    }

    onMounted(loadData);

    return {
      errorMessage,
      form,
      isLoading,
      selectTenant,
      selectedTenant,
      statusOptions,
      submit,
      successMessage,
      t,
      tenantFeatures,
      tenantStatusSeverity,
      tenantStore,
      tenants,
      toggleFeature
    };
  },
  template
});
