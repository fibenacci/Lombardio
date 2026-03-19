import { routeGuard } from ".";
import { authStore } from "../stores/auth";

describe("routeGuard", () => {
  it("allows navigation while auth initialization is still pending", () => {
    authStore.ready = false;
    authStore.token = "";
    authStore.user = null;

    expect(routeGuard({ meta: { requiresAuth: true } })).toBe(true);
  });

  it("redirects protected routes to login when unauthenticated", () => {
    authStore.ready = true;
    authStore.token = "";
    authStore.user = null;

    expect(routeGuard({ meta: { requiresAuth: true } })).toEqual({ name: "login" });
  });

  it("redirects authenticated users away from login", () => {
    authStore.ready = true;
    authStore.token = "token-123";
    authStore.user = { id: "user-admin", permissions: [], tenantId: "tenant-default" };

    expect(routeGuard({ name: "login", meta: {} })).toEqual({ path: "/app/dashboard" });
  });

  it("allows authenticated access to protected routes", () => {
    authStore.ready = true;
    authStore.token = "token-123";
    authStore.user = { id: "user-admin", permissions: [], tenantId: "tenant-default" };

    expect(routeGuard({ name: "tenant-users", meta: { requiresAuth: true } })).toBe(true);
  });

  it("redirects tenant users away from platform routes", () => {
    authStore.ready = true;
    authStore.token = "token-123";
    authStore.user = { id: "user-admin", permissions: [], tenantId: "tenant-default" };

    expect(routeGuard({ name: "platform-tenants", meta: { requiresAuth: true, requiresPlatformAccess: true } })).toEqual({
      path: "/app/dashboard"
    });
  });
});
