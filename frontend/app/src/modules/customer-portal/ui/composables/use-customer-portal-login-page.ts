import { reactive, ref } from "vue";

export function useCustomerPortalLoginPage({
  customerPortalStore,
  router,
  t
}: {
  customerPortalStore: { login: (email: string, password: string) => Promise<unknown> };
  router: { push: (payload: object) => Promise<unknown> | unknown };
  t: (key: string, params?: Record<string, unknown>) => string;
}) {
  const form = reactive({
    email: "",
    password: ""
  });
  const isSubmitting = ref(false);
  const errorMessage = ref("");

  async function submit() {
    errorMessage.value = "";
    isSubmitting.value = true;
    try {
      await customerPortalStore.login(form.email, form.password);
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
    isSubmitting,
    submit
  };
}
