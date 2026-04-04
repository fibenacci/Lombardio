import { computed, reactive, ref, onMounted } from "vue";
import { useAppToast } from "../../../../shared/ui/composables/use-app-toast";
import { useRequestFeedback } from "../../../../shared/ui/composables/use-request-feedback";
import { createHttpUsersAdapter } from "../../infrastructure/adapters/http-users.adapter";
import { createLoadUsersContextService } from "../../application/services/load-users-context.service";
import { createCreateUserService } from "../../application/services/create-user.service";
import { createUpdateUserService } from "../../application/services/update-user.service";

export function useUsersPage({
  t,
  authStore,
  tenantStore
}: {
  t: (key: string, params?: Record<string, unknown>) => string;
  authStore: any;
  tenantStore: any;
}) {
  const toast = useAppToast();
  const { errorMessage, successMessage, resetFeedback, handleError } = useRequestFeedback(t);
  const usersAdapter = createHttpUsersAdapter();
  const loadContextService = createLoadUsersContextService(usersAdapter);
  const createUserService = createCreateUserService(usersAdapter);
  const updateUserService = createUpdateUserService(usersAdapter);

  const users = ref<any[]>([]);
  const roles = ref<any[]>([]);
  const branches = ref<any[]>([]);
  const isLoading = ref(true);
  const editingUserId = ref("");
  const form = reactive({
    username: "",
    email: "",
    initialPassword: "",
    displayName: "",
    status: "ACTIVE",
    roleIds: [] as string[],
    branchIds: [] as string[]
  });

  const userRows = computed(() =>
    users.value.map((user) => ({
      ...user,
      branchCount: (user.branchIds ?? []).length,
      roleNames: (user.roleIds ?? [])
        .map((roleId: string) => roles.value.find((role) => role.id === roleId)?.displayName ?? roleId)
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

  const statusOptions = computed(() => [
    { label: t("users.status.ACTIVE"), value: "ACTIVE" },
    { label: t("users.status.INACTIVE"), value: "INACTIVE" }
  ]);

  const pageCopy = computed(() => {
    if (!tenantStore.selectedTenantId) {
      return t("users.copyWithoutTenant");
    }
    const tenantDisplayName = tenantStore.selectedTenant?.displayName || tenantStore.selectedTenantId;
    return t("users.copyWithTenant", { tenant: tenantDisplayName });
  });

  const canManageUsers = computed(() => 
    authStore.hasPermission("platform.tenants.write") || 
    (authStore.hasPermission("users.write") && tenantStore.selectedTenantId === authStore.user?.tenantId)
  );

  async function loadData() {
    isLoading.value = true;
    resetFeedback();

    if (!tenantStore.selectedTenantId) {
      users.value = [];
      roles.value = [];
      branches.value = [];
      isLoading.value = false;
      return;
    }

    try {
      const context = await loadContextService.execute(tenantStore.selectedTenantId);
      users.value = context.users;
      roles.value = context.roles;
      branches.value = context.branches;
    } catch (error) {
      handleError(error);
    } finally {
      isLoading.value = false;
    }
  }

  async function submit() {
    resetFeedback();

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

      let message = "";
      if (editingUserId.value) {
        await updateUserService.execute(editingUserId.value, payload);
        toast.success(
          t("users.messages.userUpdatedTitle"),
          t("users.messages.userUpdatedToast", { user: payload.displayName || payload.email })
        );
        message = t("users.messages.userUpdatedTitle");
      } else {
        await createUserService.execute(tenantStore.selectedTenantId, {
          email: payload.email,
          password: payload.password,
          displayName: payload.displayName,
          roles: payload.roles,
          branchIds: payload.branchIds
        });
        toast.success(
          t("users.messages.userCreatedTitle"),
          t("users.messages.userCreatedToast", { user: payload.displayName || payload.email })
        );
        message = t("users.messages.userCreatedTitle");
      }

      resetForm();
      await loadData();
      successMessage.value = message;
    } catch (error) {
      handleError(error);
    }
  }

  function canDelegateToUser(user: any) {
    const delegationAvailable = authStore.delegationAvailable;
    const canImpersonate = authStore.canImpersonate();
    const isDifferentUser = authStore.user?.id !== user.id;
    return delegationAvailable && canImpersonate && isDifferentUser;
  }

  async function delegateToUser(userId: string) {
    resetFeedback();
    try {
      await authStore.startDelegation(userId);
      await tenantStore.refreshTenants();
      await loadData();
      successMessage.value = t("users.messages.delegatedSessionStartedTitle");
      toast.info(
        t("users.messages.delegatedSessionStartedTitle"),
        t("users.messages.delegatedSessionStartedToast")
      );
    } catch (error) {
      handleError(error);
    }
  }

  function startEdit(user: any) {
    editingUserId.value = user.id;
    form.username = user.username ?? user.email;
    form.email = user.email;
    form.initialPassword = "";
    form.displayName = user.displayName;
    form.status = user.status ?? "ACTIVE";
    form.roleIds = [...(user.roleIds ?? [])];
    form.branchIds = [...(user.branchIds ?? [])];
    resetFeedback();
  }

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

  onMounted(loadData);

  return {
    branches,
    branchOptions,
    canDelegateToUser,
    canManageUsers,
    delegateToUser,
    editingUserId,
    errorMessage,
    form,
    isLoading,
    pageCopy,
    resetForm,
    roleOptions,
    roles,
    startEdit,
    statusOptions,
    submit,
    successMessage,
    t,
    tenantStore,
    userRows
  };
}
