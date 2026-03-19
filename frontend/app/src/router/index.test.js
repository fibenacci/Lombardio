import { routeGuard } from ".";
import { authStore } from "../stores/auth";
import { customerPortalStore } from "../stores/customerPortal";

describe("routeGuard", () => {
  it("allows navigation while auth initialization is still pending", () => {
    authStore.ready = false;
    authStore.token = "";
    authStore.user = null;
    customerPortalStore.ready = false;
    customerPortalStore.token = "";
    customerPortalStore.customer = null;

    expect(routeGuard({ meta: { requiresAuth: true } })).toBe(true);
  });

  it("redirects protected routes to login when unauthenticated", () => {
    authStore.ready = true;
    authStore.token = "";
    authStore.user = null;
    customerPortalStore.ready = true;
    customerPortalStore.token = "";
    customerPortalStore.customer = null;

    expect(routeGuard({ meta: { requiresAuth: true } })).toEqual({ name: "login" });
  });

  it("redirects authenticated users away from login", () => {
    authStore.ready = true;
    authStore.token = "token-123";
    authStore.user = { id: "user-admin", permissions: [], tenantId: "tenant-default" };
    customerPortalStore.ready = true;
    customerPortalStore.token = "";
    customerPortalStore.customer = null;

    expect(routeGuard({ name: "login", meta: {} })).toEqual({ path: "/app/dashboard" });
  });

  it("allows authenticated access to protected routes", () => {
    authStore.ready = true;
    authStore.token = "token-123";
    authStore.user = { id: "user-admin", permissions: [], tenantId: "tenant-default" };
    customerPortalStore.ready = true;
    customerPortalStore.token = "";
    customerPortalStore.customer = null;

    expect(routeGuard({ name: "tenant-users", meta: { requiresAuth: true } })).toBe(true);
  });

  it("redirects tenant users away from platform routes", () => {
    authStore.ready = true;
    authStore.token = "token-123";
    authStore.user = { id: "user-admin", permissions: [], tenantId: "tenant-default" };
    customerPortalStore.ready = true;
    customerPortalStore.token = "";
    customerPortalStore.customer = null;

    expect(routeGuard({ name: "platform-tenants", meta: { requiresAuth: true, requiresPlatformAccess: true } })).toEqual({
      path: "/app/dashboard"
    });
  });

  it("redirects customer portal routes to portal login when unauthenticated", () => {
    authStore.ready = true;
    authStore.token = "";
    authStore.user = null;
    customerPortalStore.ready = true;
    customerPortalStore.token = "";
    customerPortalStore.customer = null;

    expect(routeGuard({ name: "customer-portal-home", meta: { requiresCustomerPortalAuth: true } })).toEqual({
      name: "customer-portal-login"
    });
  });
});
