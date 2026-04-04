import { defineComponent, onMounted } from "vue";
import { useRoute } from "vue-router";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { useI18n } from "../../../../../app/i18n";
import { useFormatters } from "../../../../../shared/kernel/utils/formatters";
import { FormFeedback } from "../../../../../shared/ui/feedback";
import { useCustomerDetailPage } from "../../composables/use-customer-detail-page";
import {
  CustomerAmlSection,
  CustomerKycSection,
  CustomerLoansSection,
  CustomerMasterDataSection
} from "../../components";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "CustomerDetailPage",
  components: {
    CustomerAmlSection,
    CustomerKycSection,
    CustomerLoansSection,
    CustomerMasterDataSection,
    FormFeedback
  },
  setup() {
    const { t } = useI18n();
    const { formatCurrency, formatDate, formatDateTime } = useFormatters();
    const route = useRoute();
    const tenantStore = useTenantStore();

    const customerDetail = useCustomerDetailPage({
      route,
      t,
      tenantStore
    });

    onMounted(customerDetail.loadData);

    return {
      ...customerDetail,
      formatCurrency,
      formatDate,
      formatDateTime,
      t,
      tenantStore
    };
  },
  template
});
