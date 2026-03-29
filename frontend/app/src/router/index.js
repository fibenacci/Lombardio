import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { useCustomerPortalStore } from "../stores/customerPortal";

const LoginView = () => import("../views/login");
const PlatformLayout = () => import("../layouts/platform");
const TenantLayout = () => import("../layouts/tenant");
const TenantsView = () => import("../views/tenants");
const UsersView = () => import("../views/users");
const RolesView = () => import("../views/roles");
const BranchesView = () => import("../views/branches");
const TenantHomeView = () => import("../views/tenant-home");
const SecurityView = () => import("../views/security");
const CustomersView = () => import("../views/customers");
const CustomerDetailView = () => import("../views/customer-detail");
const LoansView = () => import("../views/loans");
const PawnTicketsView = () => import("../views/pawn-tickets");
const CashdeskView = () => import("../views/cashdesk");
const AuctionsView = () => import("../views/auctions");
const OnlineAuctionsView = () => import("../views/online-auctions");
const PublicAuctionView = () => import("../views/public-auction");
const CustomerPortalLoginView = () => import("../views/customer-portal-login");
const CustomerPortalActivateView = () => import("../views/customer-portal-activate");
const CustomerPortalHomeView = () => import("../views/customer-portal-home");

const routes = [
  {
    path: "/login",
    name: "login",
    component: LoginView
  },
  {
    path: "/online-auctions/:tenantId/:auctionId",
    name: "public-online-auction",
    component: PublicAuctionView
  },
  {
    path: "/portal/login",
    name: "customer-portal-login",
    component: CustomerPortalLoginView
  },
  {
    path: "/portal/activate/:token",
    name: "customer-portal-activate",
    component: CustomerPortalActivateView
  },
  {
    path: "/portal",
    redirect: "/portal/home"
  },
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
      {
        path: "",
        redirect: "/platform/tenants"
      },
      {
        path: "tenants",
        name: "platform-tenants",
        component: TenantsView
      },
      {
        path: "security",
        name: "platform-security",
        component: SecurityView
      }
    ]
  },
  {
    path: "/app",
    component: TenantLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: "",
        redirect: "/app/dashboard"
      },
      {
        path: "dashboard",
        name: "tenant-home",
        component: TenantHomeView
      },
      {
        path: "branches",
        name: "tenant-branches",
        component: BranchesView
      },
      {
        path: "customers",
        name: "tenant-customers",
        component: CustomersView
      },
      {
        path: "customers/:customerId",
        name: "tenant-customer-detail",
        component: CustomerDetailView
      },
      {
        path: "loans",
        name: "tenant-loans",
        component: LoansView
      },
      {
        path: "pawn-tickets",
        name: "tenant-pawn-tickets",
        component: PawnTicketsView
      },
      {
        path: "cashdesk",
        name: "tenant-cashdesk",
        component: CashdeskView
      },
      {
        path: "auctions",
        name: "tenant-auctions",
        component: AuctionsView
      },
      {
        path: "online-auctions",
        name: "tenant-online-auctions",
        component: OnlineAuctionsView
      },
      {
        path: "users",
        name: "tenant-users",
        component: UsersView
      },
      {
        path: "roles",
        name: "tenant-roles",
        component: RolesView
      },
      {
        path: "security",
        name: "tenant-security",
        component: SecurityView
      }
    ]
  },
  {
    path: "/",
    redirect: () => {
      const authStore = useAuthStore();
      // FIX: Access as getter, not function
      return authStore.canManagePlatform ? "/platform/tenants" : "/app/dashboard";
    }
  },
  {
    path: "/:pathMatch(.*)*",
    redirect: "/login"
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export function routeGuard(to) {
  const authStore = useAuthStore();
  const customerPortalStore = useCustomerPortalStore();

  // If we are navigating to public routes, allow immediately
  if (to.name === "public-online-auction" || (to.path && to.path.startsWith("/portal/activate"))) {
    return true;
  }

  // If auth state is not yet ready, we MUST wait for initialization
  // but in the context of our route guard, we just allow it to let the app load
  if (!authStore.ready || !customerPortalStore.ready) {
    return true;
  }

  // Handle Customer Portal Auth
  if (to.path && to.path.startsWith("/portal")) {
    if (to.meta.requiresCustomerPortalAuth && !customerPortalStore.isAuthenticated) {
      return { name: "customer-portal-login" };
    }
    return true;
  }

  // Handle Admin App Auth
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: "login" };
  }

  // If already at login but authenticated, redirect to home
  if (to.name === "login" && authStore.isAuthenticated) {
    return { path: authStore.canManagePlatform ? "/platform/tenants" : "/app/dashboard" };
  }

  // Platform access check
  if (to.meta.requiresPlatformAccess && !authStore.canManagePlatform) {
    return { path: "/app/dashboard" };
  }

  return true;
}

router.beforeEach(routeGuard);

export default router;
