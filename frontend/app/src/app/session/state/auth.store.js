import { defineStore } from "pinia";
import * as authApi from "../infrastructure/auth.api";

const AUTH_TOKEN_STORAGE_KEY = "lombardio.auth.token";
const ORIGINAL_TOKEN_STORAGE_KEY = "lombardio.auth.original_token";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    authenticated: false,
    originalToken: null,
    user: null,
    token: null,
    ready: false,
    pendingMfaChallengeId: null,
    pendingMfaMethods: []
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    currentUser: (state) => state.user,
    accessToken: (state) => state.token,
    canManagePlatform: (state) => state.user?.permissions?.includes("platform.tenants.read") || false,
    hasPermission: (state) => (permission) => state.user?.permissions?.includes(permission) || false,
    canImpersonate: (state) => () =>
      state.user?.permissions?.some((permission) => permission.startsWith("sessions.impersonate.")) || false
  },

  actions: {
    async initialize() {
      localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY);
      sessionStorage.removeItem(ORIGINAL_TOKEN_STORAGE_KEY);
      this.clearSession();

      try {
        const session = await authApi.refreshSession();
        if (session?.status === "AUTHENTICATED") {
          this.token = session.accessToken;
          this.user = session.user;
          this.authenticated = true;
        }
      } catch {
        this.clearSession();
      }

      this.ready = true;
    },

    async login(email, password) {
      try {
        const response = await authApi.login({ email, password });

        if (response.status === "MFA_REQUIRED") {
          this.pendingMfaChallengeId = response.challengeId;
          this.pendingMfaMethods = response.mfaMethods;
          return response;
        }

        this.token = response.accessToken;
        this.user = response.user ?? (await authApi.fetchCurrentUser(this.token));
        this.authenticated = true;
        this.pendingMfaChallengeId = null;
        this.pendingMfaMethods = [];
        return response;
      } catch (error) {
        this.clearSession();
        throw error;
      }
    },

    async startDelegation(userId) {
      const response = await authApi.createDelegation(userId, this.token);
      const originalToken = this.token;

      try {
        this.originalToken = originalToken;
        this.token = response.accessToken;
        this.user = await authApi.fetchCurrentUser(this.token);
        this.authenticated = true;
      } catch (error) {
        this.originalToken = null;
        this.token = originalToken;
        throw error;
      }
    },

    async endDelegation() {
      const originalToken = this.originalToken;
      if (originalToken) {
        this.token = originalToken;
        this.user = await authApi.fetchCurrentUser(this.token);
        this.originalToken = null;
      }
    },

    async logout() {
      if (this.token) {
        try {
          await authApi.logout(this.token);
        } catch {
          // Ignore logout errors.
        }
      }

      this.clearSession();
    },

    async verifyTotp(code) {
      const response = await authApi.verifyTotpChallenge({
        challengeId: this.pendingMfaChallengeId,
        code
      });

      if (response.status === "AUTHENTICATED") {
        this.token = response.accessToken;
        this.user = response.user ?? (await authApi.fetchCurrentUser(this.token));
        this.authenticated = true;
        this.pendingMfaChallengeId = null;
        this.pendingMfaMethods = [];
      }

      return response;
    },

    async beginTotpEnrollment() {
      return authApi.startTotpEnrollment(this.token);
    },

    async activateTotp(code) {
      const response = await authApi.activateTotp({ code }, this.token);
      this.user = response;
      return response;
    },

    clearSession() {
      this.authenticated = false;
      this.originalToken = null;
      this.user = null;
      this.token = "";
      this.pendingMfaChallengeId = null;
      this.pendingMfaMethods = [];
      localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY);
      sessionStorage.removeItem(ORIGINAL_TOKEN_STORAGE_KEY);
    },

    resetForTests() {
      this.authenticated = false;
      this.originalToken = null;
      this.user = null;
      this.token = "";
      this.ready = false;
      this.pendingMfaChallengeId = null;
      this.pendingMfaMethods = [];
      localStorage.clear();
      sessionStorage.clear();
    }
  }
});
