import { computed, reactive, ref, onMounted } from "vue";
import { useAppToast } from "../../../../shared/ui/composables/use-app-toast";
import { useRequestFeedback } from "../../../../shared/ui/composables/use-request-feedback";
import { createHttpTenantsAdapter } from "../../infrastructure/adapters/http-tenants.adapter";
import { createCreateTenantService } from "../../application/services/create-tenant.service";
import { createUpdateTenantFeatureService } from "../../application/services/update-tenant-feature.service";

const FEATURE_CATALOG = [
  { key: "identity-access" },
  { key: "customer-management" },
  { key: "loan-origination" },
  { key: "pawn-ticket-management" },
  { key: "aml-compliance" },
  { key: "kyc-provider-verification" },
  { key: "kyc-document-ocr" },
  { key: "auction-workflow" },
  { key: "online-auctions" },
  { key: "reporting" }
];

export function useTenantsPage({
  t,
  tenantStore
}: {
  t: (key: string, params?: Record<string, unknown>) => string;
  tenantStore: {
    tenants: any[];
    selectedTenant: any;
    selectedTenantId: string;
    features: any[];
    refreshTenants: () => Promise<void>;
    selectTenant: (id: string) => Promise<void>;
    refreshFeatures: () => Promise<void>;
  };
}) {
  const toast = useAppToast();
  const { errorMessage, successMessage, resetFeedback, handleError } = useRequestFeedback(t);
  const tenantsAdapter = createHttpTenantsAdapter();
  const createTenantService = createCreateTenantService(tenantsAdapter);
  const updateFeatureService = createUpdateTenantFeatureService(tenantsAdapter);

  const isLoading = ref(true);
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

  function tenantStatusSeverity(status: string) {
    return status === "ACTIVE" ? "success" : "contrast";
  }

  async function loadData() {
    isLoading.value = true;
    resetFeedback();

    try {
      await tenantStore.refreshTenants();
    } catch (error) {
      handleError(error, "tenants.messages.loadFailed");
    } finally {
      isLoading.value = false;
    }
  }

  async function submit() {
    resetFeedback();

    try {
      const tenant = await createTenantService.execute({
        key: form.key,
        displayName: form.displayName,
        status: form.status
      });

      await tenantStore.refreshTenants();
      await tenantStore.selectTenant(tenant.id);
      
      successMessage.value = t("tenants.messages.createdTitle");
      toast.success(t("tenants.messages.createdTitle"), t("tenants.messages.createdToast", { tenant: tenant.displayName }));
      
      form.key = "";
      form.displayName = "";
      form.status = "ACTIVE";
    } catch (error) {
      handleError(error, "tenants.messages.createFailed");
    }
  }

  async function selectTenant(tenantId: string) {
    resetFeedback();

    try {
      await tenantStore.selectTenant(tenantId);
    } catch (error) {
      handleError(error, "tenants.messages.featuresLoadFailed");
    }
  }

  async function toggleFeature(featureKey: string, enabled: boolean) {
    if (!tenantStore.selectedTenantId) {
      return;
    }

    resetFeedback();

    try {
      await updateFeatureService.execute(tenantStore.selectedTenantId, featureKey, enabled);
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
      handleError(error, "tenants.messages.featureUpdateFailed");
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
}
