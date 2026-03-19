import { defineComponent, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { fetchPortalInvitation } from "../../services/api/customerPortal";
import { customerPortalStore } from "../../stores/customerPortal";
import { useI18n } from "../../i18n";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "CustomerPortalActivateView",
  setup() {
    const route = useRoute();
    const router = useRouter();
    const { t } = useI18n();
    const invitation = ref(null);
    const isLoading = ref(true);
    const isSubmitting = ref(false);
    const errorMessage = ref("");
    const form = reactive({
      password: "",
      confirmPassword: ""
    });
    const token = String(route.params.token ?? "");

    async function loadInvitation() {
      isLoading.value = true;
      errorMessage.value = "";
      try {
        invitation.value = await fetchPortalInvitation(token);
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
      } finally {
        isLoading.value = false;
      }
    }

    async function submit() {
      if (form.password !== form.confirmPassword) {
        errorMessage.value = t("customerPortalActivate.passwordMismatch");
        return;
      }
      isSubmitting.value = true;
      errorMessage.value = "";
      try {
        await customerPortalStore.acceptInvitation(token, form.password);
        await router.push({ name: "customer-portal-home" });
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
      } finally {
        isSubmitting.value = false;
      }
    }

    onMounted(() => loadInvitation());

    return {
      errorMessage,
      form,
      invitation,
      isLoading,
      isSubmitting,
      submit,
      t
    };
  },
  template
});
