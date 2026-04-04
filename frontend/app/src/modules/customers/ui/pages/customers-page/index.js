import { computed, defineComponent, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { useI18n } from "../../../../../app/i18n";
import { useCustomersPage } from "../../composables/use-customers-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "CustomersPage",
  setup() {
    const { t } = useI18n();
    const router = useRouter();
    const tenantStore = useTenantStore();
    const customersPage = useCustomersPage({
      router,
      t,
      tenantStore
    });
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
    const pageCopy = computed(() => {
      if (!tenantStore.selectedTenantId) {
        return t("customers.copyWithoutTenant");
      }

      const tenantDisplayName = tenantStore.selectedTenant?.displayName || tenantStore.selectedTenantId;
      return t("customers.copyWithTenant", { tenant: tenantDisplayName });
    });

    onMounted(() => customersPage.loadData());

    return {
      ...customersPage,
      getAmlStatusLabel: (status) => amlStatusLabels[status]?.() ?? status ?? t("common.notAvailable"),
      getKycStatusLabel: (status) => kycStatusLabels[status]?.() ?? status ?? t("common.notAvailable"),
      pageCopy,
      t,
      tenantStore
    };
  },
  template
});
