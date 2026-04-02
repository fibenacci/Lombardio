import { ref } from "vue";
import { getRequestErrorMessage } from "../../../../shared/kernel/errors/request-error";

type TotpEnrollment = {
  qrCodeDataUrl?: string;
  secret?: string;
  recoveryCodes?: string[];
} | null;

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
    mfaEnrollmentAvailable?: boolean;
    user?: { mfaEnabled?: boolean } | null;
  };
  availableLocales: string[];
  locale: { value: string };
  setLocale: (value: string) => void;
  t: (key: string, params?: Record<string, unknown>) => string;
}) {
  const enrollment = ref<TotpEnrollment>(null);
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
      enrollment.value = (await authStore.beginTotpEnrollment()) as TotpEnrollment;
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("security.enrollmentFailed"));
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
      errorMessage.value = getRequestErrorMessage(error, t("security.activationFailed"));
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
    totpAvailable: authStore.mfaEnrollmentAvailable ?? false,
    updateLanguage
  };
}
