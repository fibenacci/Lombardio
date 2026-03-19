import { authStore } from "./auth";
import * as authApi from "../services/api/auth";

describe("authStore", () => {
  it("restores a stored session and loads the current user", async () => {
    window.localStorage.setItem("lombardio.auth.token", "token-123");
    vi.spyOn(authApi, "fetchCurrentUser").mockResolvedValue({
      id: "user-admin",
      actorUserId: "user-admin",
      tenantId: "tenant-default",
      email: "admin@lombardio.local",
      displayName: "System Admin",
      impersonating: false,
      roles: ["admin"],
      permissions: ["users.read"]
    });

    await authStore.initialize();

    expect(authStore.ready).toBe(true);
    expect(authStore.token).toBe("token-123");
    expect(authStore.user?.email).toBe("admin@lombardio.local");
  });

  it("clears invalid stored sessions", async () => {
    window.localStorage.setItem("lombardio.auth.token", "expired-token");
    vi.spyOn(authApi, "fetchCurrentUser").mockRejectedValue(new Error("Unauthorized"));

    await authStore.initialize();

    expect(authStore.ready).toBe(true);
    expect(authStore.token).toBe("");
    expect(authStore.user).toBeNull();
    expect(window.localStorage.getItem("lombardio.auth.token")).toBeNull();
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

    await authStore.endDelegation();
    expect(authStore.token).toBe("base-token");
    expect(authStore.user?.id).toBe("user-platform-admin");
  });

  it("stores a pending MFA challenge instead of a session when login requires TOTP", async () => {
    vi.spyOn(authApi, "login").mockResolvedValue({
      status: "MFA_REQUIRED",
      challengeId: "challenge-123",
      mfaMethods: ["TOTP"]
    });

    const response = await authStore.login("admin@lombardio.local", "change-me");

    expect(response.status).toBe("MFA_REQUIRED");
    expect(authStore.token).toBe("");
    expect(authStore.user).toBeNull();
    expect(authStore.pendingMfaChallengeId).toBe("challenge-123");
    expect(authStore.pendingMfaMethods).toEqual(["TOTP"]);
  });
});
