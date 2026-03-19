import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { createUser, fetchBranches, fetchRoles, fetchUsers, updateUser } from "../../services/api/access";
import { authStore } from "../../stores/auth";
import { tenantStore } from "../../stores/tenant";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "UsersView",
  setup() {
    const users = ref([]);
    const roles = ref([]);
    const branches = ref([]);
    const isLoading = ref(true);
    const errorMessage = ref("");
    const successMessage = ref("");
    const editingUserId = ref("");
    const form = reactive({
      username: "",
      email: "",
      initialPassword: "",
      displayName: "",
      status: "ACTIVE",
      roleIds: [],
      branchIds: []
    });

    const userRows = computed(() =>
      users.value.map((user) => ({
        ...user,
        roleNames: user.roleIds
          .map((roleId) => roles.value.find((role) => role.id === roleId)?.displayName ?? roleId)
          .join(", ")
      }))
    );

    async function loadData() {
      isLoading.value = true;
      errorMessage.value = "";

       if (!tenantStore.selectedTenantId) {
        users.value = [];
        roles.value = [];
        isLoading.value = false;
        return;
      }

      try {
        const [userResponse, roleResponse] = await Promise.all([
          fetchUsers(tenantStore.selectedTenantId, authStore.token),
          fetchRoles(tenantStore.selectedTenantId, authStore.token)
        ]);
        const branchResponse = await fetchBranches(tenantStore.selectedTenantId, authStore.token);

        users.value = userResponse;
        roles.value = roleResponse;
        branches.value = branchResponse;
      } catch (error) {
        handleApiError(error);
      } finally {
        isLoading.value = false;
      }
    }

    function toggleRole(roleId) {
      if (form.roleIds.includes(roleId)) {
        form.roleIds = form.roleIds.filter((item) => item !== roleId);
        return;
      }

      form.roleIds = [...form.roleIds, roleId];
    }

    function toggleBranch(branchId) {
      if (form.branchIds.includes(branchId)) {
        form.branchIds = form.branchIds.filter((item) => item !== branchId);
        return;
      }

      form.branchIds = [...form.branchIds, branchId];
    }

    async function submit() {
      successMessage.value = "";
      errorMessage.value = "";

      try {
        const payload = {
          branchIds: form.branchIds,
          username: form.username,
          email: form.email,
          displayName: form.displayName,
          status: form.status,
          roleIds: form.roleIds
        };

        if (editingUserId.value) {
          await updateUser(
            editingUserId.value,
            payload,
            authStore.token
          );
          successMessage.value = "User updated";
        } else {
          await createUser(
            tenantStore.selectedTenantId,
            {
              ...payload,
              initialPassword: form.initialPassword
            },
            authStore.token
          );
          successMessage.value = "User created";
        }

        resetForm();
        await loadData();
      } catch (error) {
        handleApiError(error);
      }
    }

    async function delegateToUser(userId) {
      successMessage.value = "";
      errorMessage.value = "";

      try {
        await authStore.startDelegation(userId);
        await tenantStore.refreshTenants();
        await loadData();
        successMessage.value = "Delegated session started";
      } catch (error) {
        handleApiError(error);
      }
    }

    function canDelegateToUser(user) {
      return authStore.canImpersonate() && authStore.user?.id !== user.id;
    }

    function startEdit(user) {
      editingUserId.value = user.id;
      form.username = user.username;
      form.email = user.email;
      form.initialPassword = "";
      form.displayName = user.displayName;
      form.status = user.status;
      form.roleIds = [...user.roleIds];
      form.branchIds = [...(user.branchIds ?? [])];
      successMessage.value = "";
      errorMessage.value = "";
    }

    const canManageUsers = computed(
      () =>
        authStore.hasPermission("platform.tenants.write")
        || (authStore.hasPermission("users.write") && tenantStore.selectedTenantId === authStore.user?.tenantId)
    );

    function resetForm() {
      editingUserId.value = "";
      form.username = "";
      form.email = "";
      form.initialPassword = "";
      form.displayName = "";
      form.status = "ACTIVE";
      form.roleIds = [];
      form.branchIds = [];
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
      editingUserId,
      errorMessage,
      form,
      isLoading,
      canManageUsers,
      canDelegateToUser,
      delegateToUser,
      resetForm,
      startEdit,
      roles,
      tenantStore,
      submit,
      successMessage,
      toggleBranch,
      toggleRole,
      userRows
    };
  },
  template
});
