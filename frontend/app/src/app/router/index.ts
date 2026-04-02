import { createRouter, createWebHistory, type RouteLocationNormalized } from "vue-router";
import { useAuthStore } from "../session/state/auth.store";
import { useCustomerPortalStore } from "../session/state/customer-portal.store";

const LoginView = () => import("../session/ui/pages/login-page");
const PlatformLayout = () => import("../layouts/platform");
const TenantLayout = () => import("../layouts/tenant");
const TenantsView = () => import("../../modules/tenants/ui/pages/tenants-page");
const UsersView = () => import("../../modules/users/ui/pages/users-page");
const RolesView = () => import("../../modules/roles/ui/pages/roles-page");
const BranchesView = () => import("../../modules/branches/ui/pages/branches-page");
const TenantHomeView = () => import("../../modules/tenant-dashboard/ui/pages/tenant-dashboard-page");
const SecurityView = () => import("../security/ui/pages/security-page");
const CustomersView = () => import("../../modules/customers/ui/pages/customers-page");
const CustomerDetailView = () => import("../../modules/customers/ui/pages/customer-detail-page");
const LoansView = () => import("../../modules/loans/ui/pages/loans-page");
const PawnTicketsView = () => import("../../modules/pawn-tickets/ui/pages/pawn-tickets-page");
const CashdeskView = () => import("../../modules/cashdesk/ui/pages/cashdesk-page");
const AuctionsView = () => import("../../modules/auctions/ui/pages/auctions-page");
const OnlineAuctionsView = () => import("../../modules/online-auctions/ui/pages/online-auctions-page");
const PublicAuctionView = () => import("../../modules/public-auctions/ui/pages/public-auction-page");
const CustomerPortalLoginView = () => import("../../modules/customer-portal/ui/pages/customer-portal-login-page");
const CustomerPortalActivateView = () => import("../../modules/customer-portal/ui/pages/customer-portal-activate-page");
const CustomerPortalHomeView = () => import("../../modules/customer-portal/ui/pages/customer-portal-home-page");

const routes = [
  { path: "/login", name: "login", component: LoginView },
  { path: "/online-auctions/:tenantId/:auctionId", name: "public-online-auction", component: PublicAuctionView },
  { path: "/portal/login", name: "customer-portal-login", component: CustomerPortalLoginView },
  { path: "/portal/activate/:token?", name: "customer-portal-activate", component: CustomerPortalActivateView },
  { path: "/portal", redirect: "/portal/home" },
  {
    path: "/portal/home",
    name: "customer-portal-home",
    component: CustomerPortalHomeView,
    meta: { requiresCustomerPortalAuth: true }
  },
  {
    path: "/platform",
    component: PlatformLayout,
    meta: { requiresAuth: true, requiresPlatformAccess: true },
    children: [
      { path: "", redirect: "/platform/tenants" },
      { path: "tenants", name: "platform-tenants", component: TenantsView },
      { path: "security", name: "platform-security", component: SecurityView }
    ]
  },
  {
    path: "/app",
    component: TenantLayout,
    meta: { requiresAuth: true },
    children: [
      { path: "", redirect: "/app/dashboard" },
      { path: "dashboard", name: "tenant-home", component: TenantHomeView },
      { path: "branches", name: "tenant-branches", component: BranchesView },
      { path: "customers", name: "tenant-customers", component: CustomersView },
      { path: "customers/:customerId", name: "tenant-customer-detail", component: CustomerDetailView },
      { path: "loans", name: "tenant-loans", component: LoansView },
      { path: "pawn-tickets", name: "tenant-pawn-tickets", component: PawnTicketsView },
      { path: "cashdesk", name: "tenant-cashdesk", component: CashdeskView },
      { path: "auctions", name: "tenant-auctions", component: AuctionsView },
      { path: "online-auctions", name: "tenant-online-auctions", component: OnlineAuctionsView },
      { path: "users", name: "tenant-users", component: UsersView },
      { path: "roles", name: "tenant-roles", component: RolesView },
      { path: "security", name: "tenant-security", component: SecurityView }
    ]
  },
  {
    path: "/",
    redirect: () => {
      const authStore = useAuthStore();
      if (!authStore.isAuthenticated) {
        return "/login";
      }
      return authStore.canManagePlatform ? "/platform/tenants" : "/app/dashboard";
    }
  },
  { path: "/:pathMatch(.*)*", redirect: "/login" }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

function isPortalActivationRoute(to: RouteLocationNormalized) {
  return Boolean(to.path?.startsWith("/portal/activate"));
}

function isPublicRoute(to: RouteLocationNormalized) {
  return to.name === "public-online-auction" || isPortalActivationRoute(to);
}

function isPortalRoute(to: RouteLocationNormalized) {
  return Boolean(to.path?.startsWith("/portal"));
}

function defaultAuthenticatedPath(authStore: ReturnType<typeof useAuthStore>) {
  return authStore.canManagePlatform ? "/platform/tenants" : "/app/dashboard";
}

export function routeGuard(to: RouteLocationNormalized) {
  const authStore = useAuthStore();
  const customerPortalStore = useCustomerPortalStore();

  if (isPublicRoute(to)) {
    return true;
  }

  if (!authStore.ready || !customerPortalStore.ready) {
    return true;
  }

  if (isPortalRoute(to)) {
    if (to.meta.requiresCustomerPortalAuth && !customerPortalStore.isAuthenticated) {
      return { name: "customer-portal-login" };
    }
    return true;
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: "login" };
  }

  if (to.name === "login" && authStore.isAuthenticated) {
    return { path: defaultAuthenticatedPath(authStore) };
  }

  if (to.meta.requiresPlatformAccess && !authStore.canManagePlatform) {
    return { path: "/app/dashboard" };
  }

  return true;
}

router.beforeEach(routeGuard);

export default router;
