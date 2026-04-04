import { ref } from "vue";
import { getRequestErrorMessage } from "../../../shared/kernel/errors/request-error";

export function useRequestFeedback(t: (key: string, params?: Record<string, unknown>) => string) {
  const errorMessage = ref("");
  const successMessage = ref("");
  const fieldErrors = ref<Array<{ field: string; message: string }>>([]);

  function resetFeedback() {
    errorMessage.value = "";
    successMessage.value = "";
    fieldErrors.value = [];
  }

  function handleError(error: unknown, fallbackKey = "common.requestFailed") {
    if (typeof error === "string") {
      errorMessage.value = error;
    } else {
      errorMessage.value = getRequestErrorMessage(error, t(fallbackKey));
    }
    fieldErrors.value = Array.isArray((error as any)?.fieldErrors)
      ? (error as any).fieldErrors
      : [];
  }

  return {
    errorMessage,
    successMessage,
    fieldErrors,
    resetFeedback,
    handleError
  };
}
