export function getRequestErrorMessage(error: unknown, fallbackMessage: string): string {
  if (typeof error === "object" && error !== null && "userMessage" in error && typeof error.userMessage === "string") {
    return error.userMessage;
  }

  if (
    typeof error === "object"
    && error !== null
    && "fieldErrors" in error
    && Array.isArray(error.fieldErrors)
    && "message" in error
    && typeof error.message === "string"
    && error.message
  ) {
    return error.message;
  }

  return fallbackMessage;
}
