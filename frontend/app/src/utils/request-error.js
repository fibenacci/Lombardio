export function getRequestErrorMessage(error, fallbackMessage) {
  return error instanceof Error ? error.message : fallbackMessage;
}
