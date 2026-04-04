import { computed, defineComponent, onMounted, ref } from "vue";
import { useI18n } from "../../../../../app/i18n";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { getRequestErrorMessage } from "../../../../../shared/kernel/errors/request-error";
import { createHttpRolesAdapter } from "../../../infrastructure/adapters/http-roles.adapter";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "RolesPage",
  setup() {
    const tenantStore = useTenantStore();
    const { t } = useI18n();
    const rolesAdapter = createHttpRolesAdapter();
    const roles = ref([]);
    const isLoading = ref(true);
    const errorMessage = ref("");
    const successMessage = ref("");

    const roleRows = computed(() =>
      roles.value.map((role) => ({
        ...role,
        permissionCount: role.permissionKeys.length
      }))
    );

    async function loadData() {
      isLoading.value = true;
      errorMessage.value = "";
      successMessage.value = "";

      if (!tenantStore.selectedTenantId) {
        roles.value = [];
        isLoading.value = false;
        return;
      }

      try {
        roles.value = await rolesAdapter.fetchRoles(tenantStore.selectedTenantId);
        if (roles.value.length === 0) {
          successMessage.value = t("roles.messages.empty");
        }
      } catch (error) {
        errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
      } finally {
        isLoading.value = false;
      }
    }

    onMounted(loadData);

    return {
      errorMessage,
      isLoading,
      reload: loadData,
      roleRows,
      successMessage,
      t,
      tenantStore
    };
  },
  template
});
