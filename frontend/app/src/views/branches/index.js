import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { createBranch, fetchBranches } from "../../services/api/access";
import { authStore } from "../../stores/auth";
import { tenantStore } from "../../stores/tenant";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "BranchesView",
  setup() {
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
        await createBranch(
          tenantStore.selectedTenantId,
          {
            key: form.key,
            displayName: form.displayName,
            status: form.status
          },
          authStore.token
        );
        successMessage.value = "Branch created";
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

      errorMessage.value = error instanceof Error ? error.message : "Request failed";
    }

    onMounted(loadData);

    return {
      branches,
      canManageBranches,
      errorMessage,
      form,
      isLoading,
      submit,
      successMessage,
      tenantStore
    };
  },
  template
});
