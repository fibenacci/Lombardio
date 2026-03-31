import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { createBranch, fetchBranches } from "../../services/api/access";
import { useAppToast } from "../../composables/use-app-toast";
import { useI18n } from "../../i18n";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "BranchesView",
  setup() {
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const toast = useAppToast();
    const { t } = useI18n();
    const branches = ref([]);
    const isLoading = ref(true);
    const errorMessage = ref("");
    const successMessage = ref("");
    const form = reactive({
      key: "",
      displayName: "",
      status: "ACTIVE"
    });
    const canManageBranches = computed(
      () =>
        authStore.hasPermission("platform.tenants.write")
        || (authStore.hasPermission("branches.write") && tenantStore.selectedTenantId === authStore.user?.tenantId)
    );

    const statusOptions = computed(() => [
      { label: t("branches.status.ACTIVE"), value: "ACTIVE" },
      { label: t("branches.status.INACTIVE"), value: "INACTIVE" }
    ]);

    async function loadData() {
      isLoading.value = true;
      errorMessage.value = "";

      if (!tenantStore.selectedTenantId) {
        branches.value = [];
        isLoading.value = false;
        return;
      }

      try {
        branches.value = await fetchBranches(tenantStore.selectedTenantId, authStore.token);
      } catch (error) {
        handleApiError(error);
      } finally {
        isLoading.value = false;
      }
    }

    async function submit() {
      successMessage.value = "";
      errorMessage.value = "";

      try {
        await createBranch(tenantStore.selectedTenantId, { ...form }, authStore.token);
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
        handleApiError(error);
      }
    }

    function handleApiError(error) {
      if (error?.status === 401) {
        authStore.clearSession();
        window.location.assign("/login");
        return;
      }

      errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
    }

    onMounted(loadData);

    return {
      branches,
      canManageBranches,
      errorMessage,
      form,
      isLoading,
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
