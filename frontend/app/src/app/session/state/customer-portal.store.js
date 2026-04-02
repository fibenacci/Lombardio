import { defineStore } from "pinia";
import {
  acceptPortalInvitation,
  fetchCustomerPortalMe,
  logoutCustomerPortal,
  refreshCustomerPortalSession,
  loginCustomerPortal
} from "../../../modules/customer-portal/infrastructure/api/customer-portal.api";

export const useCustomerPortalStore = defineStore("customerPortal", {
  state: () => ({
    customer: null,
    ready: false
  }),

  getters: {
    isAuthenticated: (state) => Boolean(state.customer)
  },

  actions: {
    async initialize() {
      this.clearSession();

      try {
        const session = await refreshCustomerPortalSession();
        if (session?.customer) {
          this.customer = session.customer;
        }
      } catch {
        this.clearSession();
      }

      this.ready = true;
    },

    async login(email, password) {
      const response = await loginCustomerPortal({ email, password });
      this.customer = response.customer;
      return response;
    },

    async acceptInvitation(token, password) {
      const response = await acceptPortalInvitation({ token, password });
      this.customer = response.customer;
      return response;
    },

    async refreshCustomer() {
      if (!this.customer) {
        this.customer = null;
        return null;
      }

      this.customer = await fetchCustomerPortalMe();
      return this.customer;
    },

    async logout() {
      try {
        await logoutCustomerPortal();
      } catch {
        // Ignore logout errors.
      }
      this.clearSession();
    },

    clearSession() {
      this.customer = null;
    },

    resetForTests() {
      this.customer = null;
      this.ready = false;
    }
  }
});
