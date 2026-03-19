import { reactive } from "vue";
import {
  acceptPortalInvitation,
  fetchCustomerPortalMe,
  loginCustomerPortal
} from "../services/api/customerPortal";

const TOKEN_STORAGE_KEY = "lombardio.customer-portal.token";

export const customerPortalStore = reactive({
  token: "",
  customer: null,
  ready: false,

  async initialize() {
    const storedToken = window.localStorage.getItem(TOKEN_STORAGE_KEY);
    if (!storedToken) {
      this.ready = true;
      return;
    }

    this.token = storedToken;
    try {
      this.customer = await fetchCustomerPortalMe(this.token);
    } catch {
      this.clearSession();
    } finally {
      this.ready = true;
    }
  },

  async login(email, password) {
    const response = await loginCustomerPortal({ email, password });
    this.token = response.accessToken;
    this.customer = response.customer;
    window.localStorage.setItem(TOKEN_STORAGE_KEY, this.token);
    return response;
  },

  async acceptInvitation(token, password) {
    const response = await acceptPortalInvitation({ token, password });
    this.token = response.accessToken;
    this.customer = response.customer;
    window.localStorage.setItem(TOKEN_STORAGE_KEY, this.token);
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
  },

  isAuthenticated() {
    return Boolean(this.token && this.customer);
  }
});
