import { defineComponent, reactive, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import { useI18n } from "../../i18n";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "LoginView",
  setup() {
    const router = useRouter();
    const authStore = useAuthStore();
    const tenantStore = useTenantStore();
    const { t } = useI18n();

    const form = reactive({
      username: "admin@lombardio.local",
      password: "admin",
      mfaCode: ""
    });
    const errorMessage = ref("");
    const isSubmitting = ref(false);

    onMounted(async () => {
      if (authStore.isAuthenticated) {
        await redirectAfterLogin();
      }
    });

    async function submit() {
      errorMessage.value = "";
      isSubmitting.value = true;

      try {
        if (authStore.pendingMfaChallengeId) {
          const response = await authStore.verifyTotp(form.mfaCode);
          if (response.status === "AUTHENTICATED") {
            await redirectAfterLogin();
          }
        } else {
          const response = await authStore.login(form.username, form.password);
          if (response.status !== "MFA_REQUIRED") {
            await redirectAfterLogin();
          }
        }
      } catch (error) {
        console.error("Login failed", error);
        errorMessage.value = t("login.loginFailed");
      } finally {
        isSubmitting.value = false;
      }
    }

    async function redirectAfterLogin() {
      await tenantStore.initialize();
      const hasPlatformPermission = authStore.canManagePlatform;
      router.push({
        path: hasPlatformPermission ? "/platform/tenants" : "/app/dashboard"
      });
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
