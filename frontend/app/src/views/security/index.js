import { defineComponent, ref } from "vue";
import { useAuthStore } from "../../stores/auth";
import { useI18n } from "../../i18n";
import template from "./template.html?raw";
import "./styles.scss";

export default defineComponent({
  name: "SecurityView",
  setup() {
    const authStore = useAuthStore();
    const { availableLocales, locale, setLocale, t } = useI18n();
    const enrollment = ref(null);
    const activationCode = ref("");
    const errorMessage = ref("");
    const successMessage = ref("");
    const selectedLocale = ref(locale.value);
    const isSubmitting = ref(false);

    async function startEnrollment() {
      errorMessage.value = "";
      successMessage.value = "";
      isSubmitting.value = true;

      try {
        enrollment.value = await authStore.beginTotpEnrollment();
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("security.enrollmentFailed");
      } finally {
        isSubmitting.value = false;
      }
    }

    async function activate() {
      errorMessage.value = "";
      successMessage.value = "";
      isSubmitting.value = true;

      try {
        await authStore.activateTotp(activationCode.value);
        successMessage.value = t("security.activationSuccess");
        enrollment.value = null;
        activationCode.value = "";
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : t("security.activationFailed");
      } finally {
        isSubmitting.value = false;
      }
    }

    function updateLanguage() {
      setLocale(selectedLocale.value);
      successMessage.value = t("language.saved");
    }

    return {
      activationCode,
      activate,
      availableLocales,
      authStore,
      enrollment,
      errorMessage,
      isSubmitting,
      locale,
      selectedLocale,
      startEnrollment,
      t,
      successMessage,
      updateLanguage
    };
  },
  template
});
