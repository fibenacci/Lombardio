import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { createRole, fetchPermissions, fetchRoles } from "../../services/api/access";
import { useAppToast } from "../../composables/use-app-toast";
import { authStore } from "../../stores/auth";
import { tenantStore } from "../../stores/tenant";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "RolesView",
  setup() {
    const toast = useAppToast();
    const roles = ref([]);
    const permissions = ref([]);
    const isLoading = ref(true);
    const errorMessage = ref("");
    const successMessage = ref("");
    const form = reactive({
      key: "",
      displayName: "",
      description: "",
      active: true,
      permissionKeys: []
    });

    const roleRows = computed(() =>
      roles.value.map((role) => ({
        ...role,
        permissionCount: role.permissionKeys.length
      }))
    );

    async function loadData() {
      isLoading.value = true;
      errorMessage.value = "";

      if (!tenantStore.selectedTenantId) {
        roles.value = [];
        permissions.value = [];
        isLoading.value = false;
        return;
      }

      try {
        const [roleResponse, permissionResponse] = await Promise.all([
          fetchRoles(tenantStore.selectedTenantId, authStore.token),
          fetchPermissions(authStore.token)
        ]);

        roles.value = roleResponse;
        permissions.value = permissionResponse;
      } catch (error) {
        handleApiError(error);
      } finally {
        isLoading.value = false;
      }
    }

    function togglePermission(permissionKey) {
      if (form.permissionKeys.includes(permissionKey)) {
        form.permissionKeys = form.permissionKeys.filter((item) => item !== permissionKey);
        return;
      }

      form.permissionKeys = [...form.permissionKeys, permissionKey];
    }

    async function submit() {
      successMessage.value = "";
      errorMessage.value = "";

      try {
        await createRole(
          tenantStore.selectedTenantId,
          {
            key: form.key,
            displayName: form.displayName,
            description: form.description,
            active: form.active,
            permissionKeys: form.permissionKeys
          },
          authStore.token
        );

        successMessage.value = "Role created";
        toast.success("Role created", `${form.displayName || form.key} is now available for assignment.`);
        resetForm();
        await loadData();
      } catch (error) {
        handleApiError(error);
      }
    }

    function resetForm() {
      form.key = "";
      form.displayName = "";
      form.description = "";
      form.active = true;
      form.permissionKeys = [];
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

    const canManageRoles = computed(
      () =>
        authStore.hasPermission("platform.tenants.write")
        || (authStore.hasPermission("roles.write") && tenantStore.selectedTenantId === authStore.user?.tenantId)
    );

    return {
      canManageRoles,
      errorMessage,
      form,
      isLoading,
      permissions,
      roleRows,
      tenantStore,
      submit,
      successMessage,
      togglePermission
    };
  },
  template
});
