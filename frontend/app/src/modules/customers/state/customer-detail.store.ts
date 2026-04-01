import { defineStore } from "pinia";

export const useCustomerDetailStore = defineStore("customer-detail-module", {
  state: () => ({
    lastVisitedCustomerId: ""
  })
});
