import router from "../router";
import { useAuthStore } from "../session/state/auth.store";
import { HttpStatus, type RequestError } from "../../shared/kernel/http/types";

export function handleGlobalError(error: unknown) {
  const requestError = error as RequestError;

  if (requestError.status === HttpStatus.UNAUTHORIZED) {
    const authStore = useAuthStore();
    authStore.clearSession();
    router.push({ name: "login" });
    return;
  }

  // Handle other global errors if needed (e.g. FORBIDDEN, INTERNAL_SERVER_ERROR)
  console.error("[Global Error Handler]", error);
}
