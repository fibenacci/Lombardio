import { ref } from "vue";
import { getRequestErrorMessage } from "../../../shared/kernel/errors/request-error";
import type { FieldError } from "../../../shared/kernel/http/types";

type ErrorWithFieldErrors = {
  fieldErrors?: unknown;
};

function isFieldError(value: unknown): value is FieldError {
  return typeof value === "object"
    && value !== null
    && "field" in value
    && typeof value.field === "string"
    && "message" in value
    && typeof value.message === "string";
}

export function useRequestFeedback(t: (key: string, params?: Record<string, unknown>) => string) {
  const errorMessage = ref("");
  const successMessage = ref("");
  const fieldErrors = ref<FieldError[]>([]);

  function resetFeedback() {
    errorMessage.value = "";
    successMessage.value = "";
    fieldErrors.value = [];
  }

  function handleError(error: unknown, fallbackKey = "common.requestFailed") {
    const errorWithFieldErrors = error as ErrorWithFieldErrors;
    if (typeof error === "string") {
      errorMessage.value = error;
    } else {
      errorMessage.value = getRequestErrorMessage(error, t(fallbackKey));
    }
    fieldErrors.value = Array.isArray(errorWithFieldErrors?.fieldErrors)
      ? errorWithFieldErrors.fieldErrors.filter(isFieldError)
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
