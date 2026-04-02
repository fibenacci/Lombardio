import { computed, defineComponent, onMounted, reactive, ref } from "vue";
import { useAppToast } from "../../../../../shared/ui/composables/use-app-toast";
import { useI18n } from "../../../../../app/i18n";
import { useAuthStore } from "../../../../../app/session/state/auth.store";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { getRequestErrorMessage } from "../../../../../shared/kernel/errors/request-error";
import { createHttpUsersAdapter } from "../../../infrastructure/adapters/http-users.adapter";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "UsersPage",
  setup() {
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const toast = useAppToast();
    const { t } = useI18n();
    const usersAdapter = createHttpUsersAdapter();
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

    function canManageTenantUsers() {
      return authStore.hasPermission("users.write") && tenantStore.selectedTenantId === authStore.user?.tenantId;
    }

    function canManagePlatformUsers() {
      return authStore.hasPermission("platform.tenants.write");
    }

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
          usersAdapter.fetchUsers(tenantStore.selectedTenantId),
          usersAdapter.fetchRoles(tenantStore.selectedTenantId)
        ]);
        const branchResponse = await usersAdapter.fetchBranches(tenantStore.selectedTenantId);

        users.value = userResponse;
        roles.value = roleResponse;
        branches.value = branchResponse;
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
          await usersAdapter.updateUser(editingUserId.value, payload);
          toast.success(
            t("users.messages.userUpdatedTitle"),
            t("users.messages.userUpdatedToast", { user: payload.displayName || payload.email })
          );
          successMessage.value = t("users.messages.userUpdatedTitle");
        } else {
          await usersAdapter.createUser(
            tenantStore.selectedTenantId,
            {
              email: payload.email,
              password: payload.password,
              displayName: payload.displayName,
              roles: payload.roles,
              branchIds: payload.branchIds
            }
          );
          toast.success(
            t("users.messages.userCreatedTitle"),
            t("users.messages.userCreatedToast", { user: payload.displayName || payload.email })
          );
          successMessage.value = t("users.messages.userCreatedTitle");
        }

        resetForm();
        await loadData();
      } catch (error) {
        errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
      }
    }

    function canDelegateToUser(user) {
      const delegationAvailable = authStore.delegationAvailable;
      const canImpersonate = authStore.canImpersonate();
      const isDifferentUser = authStore.user?.id !== user.id;

      return delegationAvailable && canImpersonate && isDifferentUser;
    }

    async function delegateToUser(userId) {
      successMessage.value = "";
      errorMessage.value = "";

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
        errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
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

    const canManageUsers = computed(() => canManagePlatformUsers() || canManageTenantUsers());

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
  },
  template
});
