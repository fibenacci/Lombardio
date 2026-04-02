import { reactive, ref } from "vue";
import { getRequestErrorMessage } from "../../../../shared/kernel/errors/request-error";
import { createHttpCustomerPortalAdapter } from "../../infrastructure/adapters/http-customer-portal.adapter";

type PortalInvitation = {
  email?: string;
  firstName?: string;
  lastName?: string;
  expiresAt?: string;
} | null;

export function useCustomerPortalActivatePage({
  customerPortalStore,
  router,
  route,
  t
}: {
  customerPortalStore: { acceptInvitation: (token: string, password: string) => Promise<unknown> };
  router: { push: (payload: object) => Promise<unknown> | unknown };
  route: { params: Record<string, unknown> };
  t: (key: string, params?: Record<string, unknown>) => string;
}) {
  const adapter = createHttpCustomerPortalAdapter();
  const invitation = ref<PortalInvitation>(null);
  const isLoading = ref(true);
  const isSubmitting = ref(false);
  const errorMessage = ref("");
  const form = reactive({
    password: "",
    confirmPassword: ""
  });
  const token = resolveActivationToken(route);

  async function loadInvitation() {
    isLoading.value = true;
    errorMessage.value = "";
    if (!token) {
      invitation.value = null;
      errorMessage.value = t("customerPortalActivate.invalidLink");
      isLoading.value = false;
      return;
    }
    try {
      invitation.value = await adapter.fetchPortalInvitation(token);
      normalizeActivationUrl();
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    } finally {
      isLoading.value = false;
    }
  }

  async function submit() {
    if (form.password !== form.confirmPassword) {
      errorMessage.value = t("customerPortalActivate.passwordMismatch");
      return;
    }
    isSubmitting.value = true;
    errorMessage.value = "";
    try {
      await customerPortalStore.acceptInvitation(token, form.password);
      await router.push({ name: "customer-portal-home" });
    } catch (error) {
      errorMessage.value = getRequestErrorMessage(error, t("common.requestFailed"));
    } finally {
      isSubmitting.value = false;
    }
  }

  return {
    errorMessage,
    form,
    invitation,
    isLoading,
    isSubmitting,
    loadInvitation,
    submit
  };
}

function resolveActivationToken(route: { params: Record<string, unknown> }) {
  const routeToken = String(route.params.token ?? "").trim();
  if (routeToken) {
    return routeToken;
  }

  const hashToken = window.location.hash.replace(/^#/, "").trim();
  return hashToken ? decodeURIComponent(hashToken) : "";
}

function normalizeActivationUrl() {
  if (!window.location.hash) {
    return;
  }

  window.history.replaceState(window.history.state, "", "/portal/activate");
}
