import { reactive } from "vue";
import {
  activateTotp,
  createDelegation,
  fetchCurrentUser,
  login as loginRequest,
  logout as logoutRequest,
  startTotpEnrollment,
  verifyTotpChallenge
} from "../services/api/auth";

const TOKEN_STORAGE_KEY = "lombardio.auth.token";
const BASE_TOKEN_STORAGE_KEY = "lombardio.auth.base-token";

export const authStore = reactive({
  token: "",
  user: null,
  ready: false,
  pendingMfaChallengeId: "",
  pendingMfaMethods: [],

  async initialize() {
    const storedToken = window.localStorage.getItem(TOKEN_STORAGE_KEY);

    if (!storedToken) {
      this.ready = true;
      return;
    }

    this.token = storedToken;

    try {
      this.user = await fetchCurrentUser(this.token);
    } catch (error) {
      this.clearSession();
    } finally {
      this.ready = true;
    }
  },

  async login(email, password) {
    if (!email || !password) {
      throw new Error("Email and password are required");
    }

    const response = await loginRequest({ email, password });

    if (response.status === "MFA_REQUIRED") {
      this.pendingMfaChallengeId = response.challengeId;
      this.pendingMfaMethods = response.mfaMethods ?? [];
      this.token = "";
      this.user = null;
      window.localStorage.removeItem(TOKEN_STORAGE_KEY);
      return response;
    }

    this.token = response.accessToken;
    this.pendingMfaChallengeId = "";
    this.pendingMfaMethods = [];
    window.localStorage.setItem(TOKEN_STORAGE_KEY, this.token);
    window.localStorage.removeItem(BASE_TOKEN_STORAGE_KEY);
    this.user = await fetchCurrentUser(this.token);
    return response;
  },

  async verifyTotp(code) {
    if (!this.pendingMfaChallengeId) {
      throw new Error("No MFA challenge pending");
    }

    const response = await verifyTotpChallenge({
      challengeId: this.pendingMfaChallengeId,
      code
    });

    this.token = response.accessToken;
    this.pendingMfaChallengeId = "";
    this.pendingMfaMethods = [];
    window.localStorage.setItem(TOKEN_STORAGE_KEY, this.token);
    this.user = await fetchCurrentUser(this.token);
    return response;
  },

  async beginTotpEnrollment() {
    if (!this.token) {
      throw new Error("Authentication required");
    }

    return startTotpEnrollment(this.token);
  },

  async activateTotp(code) {
    if (!this.token) {
      throw new Error("Authentication required");
    }

    this.user = await activateTotp({ code }, this.token);
    return this.user;
  },

  async startDelegation(userId) {
    if (!this.token) {
      throw new Error("Authentication required");
    }

    const response = await createDelegation(userId, this.token);

    if (!window.localStorage.getItem(BASE_TOKEN_STORAGE_KEY)) {
      window.localStorage.setItem(BASE_TOKEN_STORAGE_KEY, this.token);
    }

    this.token = response.accessToken;
    window.localStorage.setItem(TOKEN_STORAGE_KEY, this.token);
    this.user = await fetchCurrentUser(this.token);
    return this.user;
  },

  async endDelegation() {
    const baseToken = window.localStorage.getItem(BASE_TOKEN_STORAGE_KEY);

    if (!baseToken) {
      return null;
    }

    this.token = baseToken;
    window.localStorage.setItem(TOKEN_STORAGE_KEY, baseToken);
    window.localStorage.removeItem(BASE_TOKEN_STORAGE_KEY);
    this.user = await fetchCurrentUser(this.token);
    return this.user;
  },

  async refreshUser() {
    if (!this.token) {
      this.user = null;
      return null;
    }

    this.user = await fetchCurrentUser(this.token);
    return this.user;
  },

  async logout() {
    if (this.token) {
      try {
        await logoutRequest(this.token);
      } catch (error) {
        // Always clear client state even if the backend call fails.
      }
    }

    this.clearSession();
  },

  clearSession() {
    this.token = "";
    this.user = null;
    this.pendingMfaChallengeId = "";
    this.pendingMfaMethods = [];
    window.localStorage.removeItem(TOKEN_STORAGE_KEY);
    window.localStorage.removeItem(BASE_TOKEN_STORAGE_KEY);
  },

  resetForTests() {
    this.token = "";
    this.user = null;
    this.ready = false;
    this.pendingMfaChallengeId = "";
    this.pendingMfaMethods = [];
    window.localStorage.removeItem(TOKEN_STORAGE_KEY);
    window.localStorage.removeItem(BASE_TOKEN_STORAGE_KEY);
  },

  hasPermission(permission) {
    return this.user?.permissions?.includes(permission) ?? false;
  },

  canManagePlatform() {
    return this.hasPermission("platform.tenants.read");
  },

  canImpersonate() {
    return this.hasPermission("sessions.impersonate.platform") || this.hasPermission("sessions.impersonate.tenant");
  },

  isImpersonating() {
    return Boolean(this.user?.impersonating);
  },

  isAuthenticated() {
    return Boolean(this.token && this.user);
  },
  
  hasPendingMfa() {
    return Boolean(this.pendingMfaChallengeId);
  }
});
