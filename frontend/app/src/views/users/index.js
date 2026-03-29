import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { createUser, fetchBranches, fetchRoles, fetchUsers, updateUser } from "../../services/api/access";
import { useAppToast } from "../../composables/use-app-toast";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "UsersView",
  setup() {
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const toast = useAppToast();
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
        branchCount: (user.branchIds ?? []).length,
        roleNames: (user.roleIds ?? [])
          .map((roleId) => roles.value.find((role) => role.id === roleId)?.displayName ?? roleId)
          .join(", ")
      }))
    );

    const branchOptions = computed(() =>
      branches.value.map((branch) => ({
        label: branch.displayName,
        value: branch.id
      }))
    );

    const roleOptions = computed(() =>
      roles.value.map((role) => ({
        label: role.displayName,
        value: role.id
      }))
    );

    async function loadData() {
      isLoading.value = true;
      errorMessage.value = "";

      if (!tenantStore.selectedTenantId) {
        users.value = [];
        roles.value = [];
        branches.value = [];
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

    async function submit() {
      successMessage.value = "";
      errorMessage.value = "";

      try {
        const payload = {
          tenantId: tenantStore.selectedTenantId,
          username: form.username || form.email,
          password: form.initialPassword,
          email: form.email,
          displayName: form.displayName,
          status: form.status,
          roleIds: form.roleIds,
          roles: form.roleIds,
          branchIds: form.branchIds
        };

        if (editingUserId.value) {
          await updateUser(editingUserId.value, payload, authStore.token);
          toast.success("User updated", `${payload.displayName || payload.email} was saved.`);
          successMessage.value = "User updated";
        } else {
          await createUser(
            tenantStore.selectedTenantId,
            {
              email: payload.email,
              password: payload.password,
              displayName: payload.displayName,
              roles: payload.roles,
              branchIds: payload.branchIds
            },
            authStore.token
          );
          toast.success("User created", `${payload.displayName || payload.email} is now available in the tenant realm.`);
          successMessage.value = "User created";
        }

        resetForm();
        await loadData();
      } catch (error) {
        handleApiError(error);
      }
    }

    function canDelegateToUser(user) {
      return authStore.canImpersonate() && authStore.user?.id !== user.id;
    }

    async function delegateToUser(userId) {
      successMessage.value = "";
      errorMessage.value = "";

      try {
        await authStore.startDelegation(userId);
        await tenantStore.refreshTenants();
        await loadData();
        successMessage.value = "Delegated session started";
        toast.info("Delegated session started", "You are now acting with the selected user context.");
      } catch (error) {
        handleApiError(error);
      }
    }

    function startEdit(user) {
      editingUserId.value = user.id;
      form.username = user.username ?? user.email;
      form.email = user.email;
      form.initialPassword = "";
      form.displayName = user.displayName;
      form.status = user.status ?? "ACTIVE";
      form.roleIds = [...(user.roleIds ?? [])];
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
      branchOptions,
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
      roleOptions,
      userRows,
      tenantStore,
      submit,
      successMessage
    };
  },
  template
});
