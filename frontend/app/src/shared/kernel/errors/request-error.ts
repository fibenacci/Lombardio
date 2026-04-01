export function getRequestErrorMessage(error: unknown, fallbackMessage: string): string {
  return error instanceof Error ? error.message : fallbackMessage;
}
