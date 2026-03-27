import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { createRole, updateRole, fetchPermissions, fetchRoles } from "../../services/api/access";
import { useAppToast } from "../../composables/use-app-toast";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "RolesView",
  setup() {
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const toast = useAppToast();
    const roles = ref([]);
    const permissions = ref([]);
    const isLoading = ref(true);
    const errorMessage = ref("");
    const successMessage = ref("");
    const editingId = ref(null);
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

    const isEditing = computed(() => editingId.value !== null);

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

    function edit(role) {
      editingId.value = role.id;
      form.key = role.key;
      form.displayName = role.displayName;
      form.description = role.description;
      form.active = role.active;
      form.permissionKeys = [...role.permissionKeys];
      successMessage.value = "";
      errorMessage.value = "";
    }

    function cancelEdit() {
      editingId.value = null;
      resetForm();
    }

    async function submit() {
      successMessage.value = "";
      errorMessage.value = "";

      try {
        const payload = {
          tenantId: tenantStore.selectedTenantId,
          key: form.key,
          displayName: form.displayName,
          description: form.description,
          active: form.active,
          permissionKeys: form.permissionKeys
        };

        if (isEditing.value) {
          await updateRole(editingId.value, payload, authStore.token);
          toast.success("Role updated", `${form.displayName || form.key} has been updated.`);
        } else {
          await createRole(tenantStore.selectedTenantId, payload, authStore.token);
          toast.success("Role created", `${form.displayName || form.key} is now available for assignment.`);
        }

        cancelEdit();
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
      isEditing,
      permissions,
      roleRows,
      tenantStore,
      edit,
      cancelEdit,
      submit,
      successMessage
    };
  },
  template
});
