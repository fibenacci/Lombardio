import { defineComponent, onMounted } from "vue";
import { useTenantStore } from "../../../../../app/tenant-context/state/tenant.store";
import { useI18n } from "../../../../../app/i18n";
import { useFormatters } from "../../../../../shared/kernel/utils/formatters";
import { FormFeedback } from "../../../../../shared/ui/feedback";
import { useCashdeskPage } from "../../composables/use-cashdesk-page";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "CashdeskPage",
  components: {
    FormFeedback
  },
  setup() {
    const { t } = useI18n();
    const { formatCurrency, formatDate, formatDateTime } = useFormatters();
    const tenantStore = useTenantStore();
    const cashdeskPage = useCashdeskPage({
      t,
      tenantStore
    });

    onMounted(cashdeskPage.loadData);

    return {
      ...cashdeskPage,
      formatCurrency,
      formatDate,
      formatDateTime,
      t,
      tenantStore
    };
  },
  template
});
