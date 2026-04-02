import { defineStore } from "pinia";
import {
  acceptPortalInvitation,
  loginCustomerPortal
} from "../../../modules/customer-portal/infrastructure/api/customer-portal.api";

const TOKEN_STORAGE_KEY = "lombardio.customer-portal.token";

export const useCustomerPortalStore = defineStore("customerPortal", {
  state: () => ({
    token: "",
    customer: null,
    ready: false
  }),

  getters: {
    isAuthenticated: (state) => Boolean(state.token && state.customer)
  },

  actions: {
    async initialize() {
      window.localStorage.removeItem(TOKEN_STORAGE_KEY);
      this.ready = true;
    },

    async login(email, password) {
      const response = await loginCustomerPortal({ email, password });
      this.token = response.accessToken;
      this.customer = response.customer;
      return response;
    },

    async acceptInvitation(token, password) {
      const response = await acceptPortalInvitation({ token, password });
      this.token = response.accessToken;
      this.customer = response.customer;
      return response;
    },

    async refreshCustomer() {
      if (!this.token) {
        this.customer = null;
        return null;
      }

      this.customer = await fetchCustomerPortalMe(this.token);
      return this.customer;
    },

    logout() {
      this.clearSession();
    },

    clearSession() {
      this.token = "";
      this.customer = null;
      window.localStorage.removeItem(TOKEN_STORAGE_KEY);
    },

    resetForTests() {
      this.token = "";
      this.customer = null;
      this.ready = false;
      window.localStorage.removeItem(TOKEN_STORAGE_KEY);
    }
  }
});
