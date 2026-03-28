import { defineStore } from "pinia";
import { readRuntimeValue } from "../config/runtime-config";

const KEYCLOAK_URL = readRuntimeValue("KEYCLOAK_URL", import.meta.env.VITE_KEYCLOAK_URL ?? "http://localhost:8080");
const KEYCLOAK_REALM = readRuntimeValue("KEYCLOAK_REALM", import.meta.env.VITE_KEYCLOAK_REALM ?? "lombardio");
const KEYCLOAK_CLIENT_ID = readRuntimeValue("KEYCLOAK_CLIENT_ID", import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? "lombardio-app");

export const useAuthStore = defineStore("auth", {
  state: () => ({
    authenticated: false,
    user: null,
    token: null,
    ready: false
  }),

  getters: {
    isAuthenticated: (state) => state.authenticated,
    currentUser: (state) => state.user,
    accessToken: (state) => state.token,
    canManagePlatform: (state) => {
      return state.user?.permissions?.includes("platform.tenants.read") || false;
    }
  },

  actions: {
    async initialize() {
      const savedToken = localStorage.getItem("lombardio_token");
      const savedUser = localStorage.getItem("lombardio_user");
      
      if (savedToken && savedUser) {
        try {
          this.token = savedToken;
          this.user = JSON.parse(savedUser);
          this.authenticated = true;
        } catch (e) {
          this.clearSession();
        }
      }
      this.ready = true;
    },

    async login(username, password) {
      const params = new URLSearchParams();
      params.append("client_id", KEYCLOAK_CLIENT_ID);
      params.append("grant_type", "password");
      params.append("username", username);
      params.append("password", password);

      try {
        const response = await fetch(`${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`, {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: params
        });

        if (!response.ok) throw new Error("Invalid credentials");

        const data = await response.json();
        this.token = data.access_token;
        this.authenticated = true;

        // Extract user info from JWT payload
        const payload = JSON.parse(atob(this.token.split(".")[1]));
        this.user = {
          id: payload.sub,
          username: payload.preferred_username,
          email: payload.email,
          displayName: payload.name || payload.preferred_username,
          tenantId: payload.tenantId || "tenant-default", // Fallback if not in token
          permissions: payload.realm_access?.roles || []
        };

        localStorage.setItem("lombardio_token", this.token);
        localStorage.setItem("lombardio_user", JSON.stringify(this.user));
      } catch (error) {
        this.clearSession();
        throw error;
      }
    },

    async logout() {
      this.clearSession();
    },

    clearSession() {
      this.authenticated = false;
      this.user = null;
      this.token = null;
      localStorage.removeItem("lombardio_token");
      localStorage.removeItem("lombardio_user");
    },

    resetForTests() {
      this.authenticated = false;
      this.user = null;
      this.token = null;
      this.ready = false;
      localStorage.clear();
    }
  }
});
