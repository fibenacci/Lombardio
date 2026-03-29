import { defineStore } from "pinia";
import * as authApi from "../services/api/auth";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    authenticated: false,
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
    canManagePlatform: (state) => {
      return state.user?.permissions?.includes("platform.tenants.read") || false;
    },
    hasPermission: (state) => (permission) => {
      return state.user?.permissions?.includes(permission) || false;
    },
    canImpersonate: (state) => () => {
      return state.user?.permissions?.some((p) => p.startsWith("sessions.impersonate.")) || false;
    }
  },

  actions: {
    async initialize() {
      const savedToken = localStorage.getItem("lombardio.auth.token");
      
      if (savedToken) {
        try {
          this.token = savedToken;
          this.user = await authApi.fetchCurrentUser(this.token);
          this.authenticated = true;
        } catch (e) {
          this.clearSession();
        }
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
        this.user = await authApi.fetchCurrentUser(this.token);
        this.authenticated = true;
        this.pendingMfaChallengeId = null;
        this.pendingMfaMethods = [];

        localStorage.setItem("lombardio.auth.token", this.token);
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
        this.token = response.accessToken;
        this.user = await authApi.fetchCurrentUser(this.token);
        this.authenticated = true;
        
        // Save the original token to be able to end delegation later
        // In a real app, we might just store it in memory or another storage
        sessionStorage.setItem("lombardio.auth.original_token", originalToken);
      } catch (error) {
        this.token = originalToken;
        throw error;
      }
    },

    async endDelegation() {
      const originalToken = sessionStorage.getItem("lombardio.auth.original_token");
      if (originalToken) {
        this.token = originalToken;
        this.user = await authApi.fetchCurrentUser(this.token);
        sessionStorage.removeItem("lombardio.auth.original_token");
      }
    },

    async logout() {
      if (this.token) {
        try {
          await authApi.logout(this.token);
        } catch (e) {
          // Ignore logout errors
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
        this.user = await authApi.fetchCurrentUser(this.token);
        this.authenticated = true;
        this.pendingMfaChallengeId = null;
        this.pendingMfaMethods = [];
        localStorage.setItem("lombardio.auth.token", this.token);
      }
      return response;
    },

    async beginTotpEnrollment() {
      return await authApi.startTotpEnrollment(this.token);
    },

    async activateTotp(code) {
      const response = await authApi.activateTotp({ code }, this.token);
      this.user = response;
      return response;
    },

    clearSession() {
      this.authenticated = false;
      this.user = null;
      this.token = "";
      this.pendingMfaChallengeId = null;
      this.pendingMfaMethods = [];
      localStorage.removeItem("lombardio.auth.token");
      sessionStorage.removeItem("lombardio.auth.original_token");
    },

    resetForTests() {
      this.authenticated = false;
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
