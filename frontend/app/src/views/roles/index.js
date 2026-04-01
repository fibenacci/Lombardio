import { computed, defineComponent, onMounted, ref } from "vue";
import { fetchRoles } from "../../services/api/access";
import { useI18n } from "../../i18n";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import { getRequestErrorMessage } from "../../utils/request-error";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "RolesView",
  setup() {
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const { t } = useI18n();
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
        roles.value = await fetchRoles(tenantStore.selectedTenantId, authStore.token);
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
      roleRows,
      successMessage,
      t,
      tenantStore,
      reload: loadData
    };
  },
  template
});
