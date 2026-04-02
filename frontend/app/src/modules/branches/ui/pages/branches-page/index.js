import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { useAppToast } from "../../../../../shared/ui/composables/use-app-toast";
import { useI18n } from "../../../../../app/i18n";
import { useAuthStore } from "../../../../../app/session/state/auth.store";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { getRequestErrorMessage } from "../../../../../shared/kernel/errors/request-error";
import { createHttpBranchesAdapter } from "../../../infrastructure/adapters/http-branches.adapter";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "BranchesPage",
  setup() {
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const toast = useAppToast();
    const { t } = useI18n();
    const branchesAdapter = createHttpBranchesAdapter();
    const branches = ref([]);
    const isLoading = ref(true);
    const errorMessage = ref("");
    const successMessage = ref("");
    const form = reactive({
      key: "",
      displayName: "",
      status: "ACTIVE"
    });

    function canManageTenantBranches() {
      return authStore.hasPermission("branches.write") && tenantStore.selectedTenantId === authStore.user?.tenantId;
    }

    function canManagePlatformBranches() {
      return authStore.hasPermission("platform.tenants.write");
    }

    const canManageBranches = computed(() => canManagePlatformBranches() || canManageTenantBranches());

    const statusOptions = computed(() => [
      { label: t("branches.status.ACTIVE"), value: "ACTIVE" },
      { label: t("branches.status.INACTIVE"), value: "INACTIVE" }
    ]);
    const pageCopy = computed(() => {
      if (!tenantStore.selectedTenantId) {
        return t("branches.copyWithoutTenant");
      }

      const tenantDisplayName = tenantStore.selectedTenant?.displayName || tenantStore.selectedTenantId;
      return t("branches.copyWithTenant", { tenant: tenantDisplayName });
    });

    async function loadData() {
      isLoading.value = true;
      errorMessage.value = "";

      if (!tenantStore.selectedTenantId) {
        branches.value = [];
        isLoading.value = false;
        return;
      }

      try {
        branches.value = await branchesAdapter.fetchBranches(tenantStore.selectedTenantId);
      } catch (error) {
        errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
      } finally {
        isLoading.value = false;
      }
    }

    async function submit() {
      successMessage.value = "";
      errorMessage.value = "";

      try {
        await branchesAdapter.createBranch(tenantStore.selectedTenantId, { ...form });
        toast.success(
          t("branches.messages.branchCreatedTitle"),
          t("branches.messages.branchCreatedToast", { displayName: form.displayName })
        );
        successMessage.value = t("branches.messages.branchCreatedTitle");
        form.key = "";
        form.displayName = "";
        form.status = "ACTIVE";
        await loadData();
      } catch (error) {
        errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
      }
    }

    onMounted(loadData);

    return {
      branches,
      canManageBranches,
      errorMessage,
      form,
      isLoading,
      pageCopy,
      reload: loadData,
      statusOptions,
      successMessage,
      submit,
      t,
      tenantStore
    };
  },
  template
});
