import { defineComponent, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useCustomerPortalStore } from "../../stores/customerPortal";
import { useI18n } from "../../i18n";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "CustomerPortalLoginView",
  setup() {
    const customerPortalStore = useCustomerPortalStore();
    const router = useRouter();
    const { t } = useI18n();
    const form = reactive({
      email: "",
      password: ""
    });
    const isSubmitting = ref(false);
    const errorMessage = ref("");

    async function submit() {
      errorMessage.value = "";
      isSubmitting.value = true;
      try {
        await customerPortalStore.login(form.email, form.password);
        await router.push({ name: "customer-portal-home" });
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
      } finally {
        isSubmitting.value = false;
      }
    }

    return {
      errorMessage,
      form,
      isSubmitting,
      submit,
      t
    };
  },
  template
});
