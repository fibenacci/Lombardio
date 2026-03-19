import { createRouter, createWebHistory } from "vue-router";
import LoginView from "../views/login";
import PlatformLayout from "../layouts/platform";
import TenantLayout from "../layouts/tenant";
import TenantsView from "../views/tenants";
import UsersView from "../views/users";
import RolesView from "../views/roles";
import BranchesView from "../views/branches";
import TenantHomeView from "../views/tenant-home";
import SecurityView from "../views/security";
import CustomersView from "../views/customers";
import CustomerDetailView from "../views/customer-detail";
import LoansView from "../views/loans";
import PawnTicketsView from "../views/pawn-tickets";
import CashdeskView from "../views/cashdesk";
import AuctionsView from "../views/auctions";
import OnlineAuctionsView from "../views/online-auctions";
import PublicAuctionView from "../views/public-auction";
import { authStore } from "../stores/auth";

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
    redirect: () => defaultRoute()
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

function defaultRoute() {
  return authStore.canManagePlatform() ? "/platform/tenants" : "/app/dashboard";
}

export function routeGuard(to) {
  if (!authStore.ready) {
    return true;
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated()) {
    return { name: "login" };
  }

  if (to.name === "login" && authStore.isAuthenticated()) {
    return { path: defaultRoute() };
  }

  if (to.meta.requiresPlatformAccess && !authStore.canManagePlatform()) {
    return { path: "/app/dashboard" };
  }

  return true;
}

router.beforeEach(routeGuard);

export default router;
