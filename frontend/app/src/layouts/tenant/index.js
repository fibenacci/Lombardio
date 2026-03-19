import { computed, defineComponent, ref, watch } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { authStore } from "../../stores/auth";
import { useI18n } from "../../i18n";
import { tenantStore } from "../../stores/tenant";
import template from "./template.html?raw";
import "./styles.scss";

const NAV_GROUPS = [
  {
    key: "workspace",
    labelKey: "tenantLayout.workspace",
    items: [
      { name: "tenant-home", labelKey: "tenantLayout.dashboard", to: "/app/dashboard" }
    ]
  },
  {
    key: "operations",
    labelKey: "tenantLayout.operations",
    items: [
      { name: "tenant-customers", labelKey: "tenantLayout.customers", to: "/app/customers" },
      { name: "tenant-loans", labelKey: "tenantLayout.loans", to: "/app/loans" },
      { name: "tenant-pawn-tickets", labelKey: "tenantLayout.pawnTickets", to: "/app/pawn-tickets" },
      { name: "tenant-cashdesk", labelKey: "tenantLayout.cashdesk", to: "/app/cashdesk" },
      { name: "tenant-auctions", labelKey: "tenantLayout.auctions", to: "/app/auctions" }
    ]
  },
  {
    key: "live",
    labelKey: "tenantLayout.live",
    items: [
      { name: "tenant-online-auctions", labelKey: "tenantLayout.onlineAuctions", to: "/app/online-auctions" }
    ]
  },
  {
    key: "admin",
    labelKey: "tenantLayout.admin",
    items: [
      { name: "tenant-branches", labelKey: "tenantLayout.branches", to: "/app/branches" },
      { name: "tenant-users", labelKey: "tenantLayout.users", to: "/app/users" },
      { name: "tenant-roles", labelKey: "tenantLayout.roles", to: "/app/roles" },
      { name: "tenant-security", labelKey: "tenantLayout.security", to: "/app/security" }
    ]
  }
];

function findActiveGroupKey(routeName) {
  return NAV_GROUPS.find((group) => group.items.some((item) => item.name === routeName))?.key ?? "workspace";
}

export default defineComponent({
  name: "TenantLayout",
  components: { RouterLink, RouterView },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const { t } = useI18n();
    const openGroupKey = ref(findActiveGroupKey(route.name));
    const isSidebarCollapsed = ref(false);
    const user = computed(() => authStore.user);
    const canManagePlatform = computed(() => authStore.canManagePlatform());
    const selectedTenant = computed(() => tenantStore.selectedTenant());
    const isImpersonating = computed(() => authStore.isImpersonating());
    const navGroups = computed(() =>
      NAV_GROUPS.map((group) => ({
        ...group,
        label: t(group.labelKey),
        items: group.items.map((item) => ({
          ...item,
          label: t(item.labelKey)
        })),
        isActive: group.items.some((item) => item.name === route.name)
      }))
    );

    watch(
      () => route.name,
      (routeName) => {
        openGroupKey.value = findActiveGroupKey(routeName);
      },
      { immediate: true }
    );

    function toggleGroup(groupKey) {
      openGroupKey.value = openGroupKey.value === groupKey ? null : groupKey;
    }

    function isGroupOpen(groupKey) {
      return !isSidebarCollapsed.value && openGroupKey.value === groupKey;
    }

    function toggleSidebar() {
      isSidebarCollapsed.value = !isSidebarCollapsed.value;
    }

    async function endDelegation() {
      await authStore.endDelegation();
      await tenantStore.refreshTenants();
      router.push({ path: canManagePlatform.value ? "/platform/tenants" : "/app/dashboard" });
    }

    async function logout() {
      await authStore.logout();
      router.push({ name: "login" });
    }

    return {
      canManagePlatform,
      endDelegation,
      isImpersonating,
      isGroupOpen,
      isSidebarCollapsed,
      navGroups,
      logout,
      route,
      selectedTenant,
      t,
      toggleSidebar,
      toggleGroup,
      user
    };
  },
  template
});
