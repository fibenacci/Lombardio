import { defineComponent, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { authStore } from "../../stores/auth";
import { tenantStore } from "../../stores/tenant";
import { useI18n } from "../../i18n";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "LoginView",
  setup() {
    const router = useRouter();
    const { t } = useI18n();
    const form = reactive({
      email: "admin@lombardio.local",
      password: "change-me",
      totpCode: ""
    });
    const errorMessage = ref("");
    const isSubmitting = ref(false);

    async function submit() {
      errorMessage.value = "";
      isSubmitting.value = true;

      try {
        if (authStore.hasPendingMfa()) {
          await authStore.verifyTotp(form.totpCode);
        } else {
          await authStore.login(form.email, form.password);
        }

        if (authStore.hasPendingMfa()) {
          return;
        }

        await tenantStore.initialize();

        router.push({
          path: authStore.canManagePlatform() ? "/platform/tenants" : "/app/dashboard"
        });
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("login.loginFailed");
      } finally {
        isSubmitting.value = false;
      }
    }

    return {
      errorMessage,
      form,
      isSubmitting,
      authStore,
      submit,
      t
    };
  },
  template
});
