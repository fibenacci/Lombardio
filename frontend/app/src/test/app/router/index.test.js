import { routeGuard } from "../../../app/router";
import { useAuthStore, useCustomerPortalStore } from "../../../app/session/state";

describe("routeGuard", () => {
  let authStore;
  let customerPortalStore;

  beforeEach(() => {
    authStore = useAuthStore();
    customerPortalStore = useCustomerPortalStore();
  });

  it("allows navigation while auth initialization is still pending", () => {
    authStore.ready = false;
    authStore.authenticated = false;
    authStore.user = null;
    customerPortalStore.ready = false;
    customerPortalStore.customer = null;

    expect(routeGuard({ path: "/app/dashboard", meta: { requiresAuth: true } })).toBe(true);
  });

  it("redirects protected routes to login when unauthenticated", () => {
    authStore.ready = true;
    authStore.authenticated = false;
    authStore.user = null;
    customerPortalStore.ready = true;
    customerPortalStore.customer = null;

    expect(routeGuard({ path: "/app/dashboard", meta: { requiresAuth: true } })).toEqual({ name: "login" });
  });

  it("redirects authenticated users away from login", () => {
    authStore.ready = true;
    authStore.authenticated = true;
    authStore.user = { id: "user-admin", permissions: [], tenantId: "tenant-default" };
    customerPortalStore.ready = true;
    customerPortalStore.customer = null;

    expect(routeGuard({ path: "/login", name: "login", meta: {} })).toEqual({ path: "/app/dashboard" });
  });

  it("allows authenticated access to protected routes", () => {
    authStore.ready = true;
    authStore.authenticated = true;
    authStore.user = { id: "user-admin", permissions: [], tenantId: "tenant-default" };
    customerPortalStore.ready = true;
    customerPortalStore.customer = null;

    expect(routeGuard({ path: "/app/users", name: "tenant-users", meta: { requiresAuth: true } })).toBe(true);
  });

  it("redirects tenant users away from platform routes", () => {
    authStore.ready = true;
    authStore.authenticated = true;
    authStore.user = { id: "user-admin", permissions: [], tenantId: "tenant-default" };
    customerPortalStore.ready = true;
    customerPortalStore.customer = null;

    expect(routeGuard({ path: "/platform/tenants", name: "platform-tenants", meta: { requiresAuth: true, requiresPlatformAccess: true } })).toEqual({
      path: "/app/dashboard"
    });
  });

  it("redirects customer portal routes to portal login when unauthenticated", () => {
    authStore.ready = true;
    authStore.authenticated = false;
    authStore.user = null;
    customerPortalStore.ready = true;
    customerPortalStore.customer = null;

    expect(routeGuard({ path: "/portal/home", name: "customer-portal-home", meta: { requiresCustomerPortalAuth: true } })).toEqual({
      name: "customer-portal-login"
    });
  });
});
