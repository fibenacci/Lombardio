import { ref } from "vue";

export function useSecurityPage({
  authStore,
  availableLocales,
  locale,
  setLocale,
  t
}: {
  authStore: {
    activateTotp: (code: string) => Promise<unknown>;
    beginTotpEnrollment: () => Promise<unknown>;
    user?: { mfaEnabled?: boolean } | null;
  };
  availableLocales: string[];
  locale: { value: string };
  setLocale: (value: string) => void;
  t: (key: string, params?: Record<string, unknown>) => string;
}) {
  const enrollment = ref<any | null>(null);
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
    authStore,
    availableLocales,
    enrollment,
    errorMessage,
    isSubmitting,
    locale,
    selectedLocale,
    startEnrollment,
    successMessage,
    updateLanguage
  };
}
