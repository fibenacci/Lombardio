import { computed, defineComponent, ref, watch } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import { useI18n } from "../../i18n";
import template from "./template.html?raw";
import "./styles.scss";

const NAV_GROUPS = [
  {
    key: "workspace",
    labelKey: "tenantLayout.workspace",
    icon: "pi pi-home",
    items: [
      { name: "tenant-home", labelKey: "tenantLayout.dashboard", to: "/app/dashboard", icon: "pi pi-chart-line" }
    ]
  },
  {
    key: "operations",
    labelKey: "tenantLayout.operations",
    icon: "pi pi-briefcase",
    items: [
      { name: "tenant-customers", labelKey: "tenantLayout.customers", to: "/app/customers", icon: "pi pi-users" },
      { name: "tenant-loans", labelKey: "tenantLayout.loans", to: "/app/loans", icon: "pi pi-wallet" },
      { name: "tenant-pawn-tickets", labelKey: "tenantLayout.pawnTickets", to: "/app/pawn-tickets", icon: "pi pi-ticket" },
      { name: "tenant-cashdesk", labelKey: "tenantLayout.cashdesk", to: "/app/cashdesk", icon: "pi pi-credit-card" },
      { name: "tenant-auctions", labelKey: "tenantLayout.auctions", to: "/app/auctions", icon: "pi pi-megaphone" }
    ]
  },
  {
    key: "live",
    labelKey: "tenantLayout.live",
    icon: "pi pi-bolt",
    items: [
      { name: "tenant-online-auctions", labelKey: "tenantLayout.onlineAuctions", to: "/app/online-auctions", icon: "pi pi-globe" }
    ]
  },
  {
    key: "admin",
    labelKey: "tenantLayout.admin",
    icon: "pi pi-cog",
    items: [
      { name: "tenant-branches", labelKey: "tenantLayout.branches", to: "/app/branches", icon: "pi pi-building-columns" },
      { name: "tenant-users", labelKey: "tenantLayout.users", to: "/app/users", icon: "pi pi-user" },
      { name: "tenant-roles", labelKey: "tenantLayout.roles", to: "/app/roles", icon: "pi pi-id-card" },
      { name: "tenant-security", labelKey: "tenantLayout.security", to: "/app/security", icon: "pi pi-shield" }
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
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();

    const openGroupKey = ref(findActiveGroupKey(route.name));
    const isSidebarCollapsed = ref(false);
    
    const user = computed(() => authStore.currentUser);
    const canManagePlatform = computed(() => authStore.canManagePlatform);
    const selectedTenant = computed(() => tenantStore.selectedTenant);
    const isImpersonating = computed(() => authStore.user?.impersonating || false);
    
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

    function groupInitial(label) {
      return label.slice(0, 1).toUpperCase();
    }

    function toggleSidebar() {
      isSidebarCollapsed.value = !isSidebarCollapsed.value;
    }

    async function endDelegation() {
      // Logic for ending delegation if needed
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
      groupInitial,
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
