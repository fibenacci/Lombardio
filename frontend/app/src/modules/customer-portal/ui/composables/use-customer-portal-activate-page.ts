import { reactive, ref } from "vue";
import { createHttpCustomerPortalAdapter } from "../../infrastructure/adapters/http-customer-portal.adapter";

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
  const invitation = ref<any | null>(null);
  const isLoading = ref(true);
  const isSubmitting = ref(false);
  const errorMessage = ref("");
  const form = reactive({
    password: "",
    confirmPassword: ""
  });
  const token = String(route.params.token ?? "");

  async function loadInvitation() {
    isLoading.value = true;
    errorMessage.value = "";
    try {
      invitation.value = await adapter.fetchPortalInvitation(token);
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
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
      errorMessage.value = error instanceof Error ? error.message : t("common.requestFailed");
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
