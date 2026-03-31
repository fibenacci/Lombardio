import { defineComponent, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { searchCustomers } from "../../services/api/customer";
import { fetchAmlStatus } from "../../services/api/aml";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import { useI18n } from "../../i18n";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "CustomersView",
  setup() {
    const { t } = useI18n();
    const router = useRouter();
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    
    const customers = ref([]);
    const query = ref("");
    const isLoading = ref(true);
    const errorMessage = ref("");
    const kycStatusLabels = {
      NOT_STARTED: () => t("customerDetail.statusOptions.kyc.NOT_STARTED"),
      IN_PROGRESS: () => t("customerDetail.statusOptions.kyc.IN_PROGRESS"),
      APPROVED: () => t("customerDetail.statusOptions.kyc.APPROVED"),
      REJECTED: () => t("customerDetail.statusOptions.kyc.REJECTED")
    };
    const amlStatusLabels = {
      NOT_REVIEWED: () => t("customerDetail.statusOptions.aml.NOT_REVIEWED"),
      CLEAR: () => t("customerDetail.statusOptions.aml.CLEAR"),
      REVIEW_REQUIRED: () => t("customerDetail.statusOptions.aml.REVIEW_REQUIRED"),
      BLOCKED: () => t("customerDetail.statusOptions.aml.BLOCKED"),
      REPORTED: () => t("customerDetail.statusOptions.aml.REPORTED"),
      UNKNOWN: () => t("customers.amlUnknown")
    };

    async function loadAmlSnapshot(tenantId, customer) {
      try {
        const aml = await fetchAmlStatus(tenantId, customer.id, authStore.token);
        return {
          ...customer,
          amlStatus: aml.status,
          amlRiskLevel: aml.riskLevel,
          amlDecisionReason: aml.decisionReason,
          amlOriginationAllowed: aml.originationAllowed,
          suspiciousActivityReported: aml.suspiciousActivityReported,
          goamlReference: aml.goamlReference
        };
      } catch {
        return {
          ...customer,
          amlStatus: "UNKNOWN",
          amlRiskLevel: null,
          amlDecisionReason: t("customers.amlLoadFailed"),
          amlOriginationAllowed: false,
          suspiciousActivityReported: false,
          goamlReference: null
        };
      }
    }

    async function loadData(searchQuery = "") {
      isLoading.value = true;
      errorMessage.value = "";

      if (!tenantStore.selectedTenantId) {
        customers.value = [];
        isLoading.value = false;
        return;
      }

      try {
        const tenantId = tenantStore.selectedTenantId;
        const customerResponse = await searchCustomers(tenantId, searchQuery, authStore.token);
        customers.value = await Promise.all(customerResponse.map((customer) => loadAmlSnapshot(tenantId, customer)));
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
      } finally {
        isLoading.value = false;
      }
    }

    async function runSearch() {
      await loadData(query.value);
    }

    function openCustomer(customerId) {
      if (!customerId) {
        return;
      }
      router.push({ name: "tenant-customer-detail", params: { customerId } });
    }

    onMounted(() => loadData());

    return {
      customers,
      errorMessage,
      getAmlStatusLabel: (status) => amlStatusLabels[status]?.() ?? status ?? t("common.notAvailable"),
      getKycStatusLabel: (status) => kycStatusLabels[status]?.() ?? status ?? t("common.notAvailable"),
      isLoading,
      openCustomer,
      query,
      runSearch,
      t,
      tenantStore
    };
  },
  template
});
