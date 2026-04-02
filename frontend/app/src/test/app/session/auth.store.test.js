import { useAuthStore } from "../../../app/session/state";
import * as authApi from "../../../app/session/infrastructure/auth.api";

describe("authStore", () => {
  let authStore;

  beforeEach(() => {
    authStore = useAuthStore();
  });

  it("does not restore persisted sessions from browser storage", async () => {
    window.localStorage.setItem("lombardio.auth.token", "token-123");
    vi.spyOn(authApi, "refreshSession").mockResolvedValue(null);

    await authStore.initialize();

    expect(authStore.ready).toBe(true);
    expect(authStore.user).toBeNull();
    expect(window.localStorage.getItem("lombardio.auth.token")).toBeNull();
  });

  it("removes stale persisted tokens from earlier frontend versions", async () => {
    window.localStorage.setItem("lombardio.auth.token", "expired-token");
    window.sessionStorage.setItem("lombardio.auth.original_token", "delegation-token");
    vi.spyOn(authApi, "refreshSession").mockResolvedValue(null);

    await authStore.initialize();

    expect(authStore.ready).toBe(true);
    expect(authStore.user).toBeNull();
    expect(window.localStorage.getItem("lombardio.auth.token")).toBeNull();
    expect(window.sessionStorage.getItem("lombardio.auth.original_token")).toBeNull();
  });

  it("restores a server-backed operator session during initialize", async () => {
    vi.spyOn(authApi, "refreshSession").mockResolvedValue({
      status: "AUTHENTICATED",
      user: {
        id: "user-admin",
        actorUserId: "user-admin",
        tenantId: "tenant-default",
        email: "admin@lombardio.local",
        displayName: "System Admin",
        impersonating: false,
        roles: ["users.read"],
        permissions: ["users.read"]
      }
    });

    await authStore.initialize();

    expect(authStore.ready).toBe(true);
    expect(authStore.user?.id).toBe("user-admin");
    expect(authStore.isAuthenticated).toBe(true);
  });

  it("starts and ends a delegated session", async () => {
    authStore.token = "base-token";
    authStore.user = {
      id: "user-platform-admin",
      actorUserId: "user-platform-admin",
      tenantId: "tenant-platform",
      permissions: ["sessions.impersonate.platform", "platform.tenants.read"],
      impersonating: false
    };

    vi.spyOn(authApi, "createDelegation").mockResolvedValue({
      accessToken: "delegated-token"
    });
    vi.spyOn(authApi, "fetchCurrentUser")
      .mockResolvedValueOnce({
        id: "user-admin",
        actorUserId: "user-platform-admin",
        tenantId: "tenant-default",
        email: "admin@lombardio.local",
        displayName: "System Admin",
        impersonating: true,
        roles: ["admin"],
        permissions: ["users.read", "users.write"]
      })
      .mockResolvedValueOnce({
        id: "user-platform-admin",
        actorUserId: "user-platform-admin",
        tenantId: "tenant-platform",
        email: "platform@lombardio.local",
        displayName: "Platform Admin",
        impersonating: false,
        roles: ["platform-admin"],
        permissions: ["platform.tenants.read", "sessions.impersonate.platform"]
      });

    await authStore.startDelegation("user-admin");
    expect(authStore.token).toBe("delegated-token");
    expect(authStore.user?.impersonating).toBe(true);
    expect(authStore.originalToken).toBe("base-token");

    await authStore.endDelegation();
    expect(authStore.token).toBe("base-token");
    expect(authStore.user?.id).toBe("user-platform-admin");
    expect(authStore.originalToken).toBeNull();
  });

  it("stores a pending MFA challenge instead of a session when login requires TOTP", async () => {
    vi.spyOn(authApi, "login").mockResolvedValue({
      status: "MFA_REQUIRED",
      challengeId: "challenge-123",
      mfaMethods: ["TOTP"]
    });

    const response = await authStore.login("admin@lombardio.local", "change-me");

    expect(response.status).toBe("MFA_REQUIRED");
    expect(authStore.user).toBeNull();
    expect(authStore.pendingMfaChallengeId).toBe("challenge-123");
    expect(authStore.pendingMfaMethods).toEqual(["TOTP"]);
  });
});
