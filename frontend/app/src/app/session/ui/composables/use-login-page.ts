import { onMounted, reactive, ref } from "vue";

export function useLoginPage({
  authStore,
  router,
  t,
  tenantStore
}: {
  authStore: {
    canManagePlatform: boolean;
    isAuthenticated: boolean;
    login: (username: string, password: string) => Promise<{ status: string }>;
    pendingMfaChallengeId: string;
    verifyTotp: (code: string) => Promise<{ status: string }>;
  };
  router: { push: (payload: object) => Promise<unknown> | unknown };
  t: (key: string, params?: Record<string, unknown>) => string;
  tenantStore: { initialize: () => Promise<unknown> };
}) {
  const form = reactive({
    username: "admin@lombardio.local",
    password: "admin",
    mfaCode: ""
  });
  const errorMessage = ref("");
  const isSubmitting = ref(false);

  onMounted(async () => {
    if (authStore.isAuthenticated) {
      await redirectAfterLogin();
    }
  });

  async function submit() {
    errorMessage.value = "";
    isSubmitting.value = true;

    try {
      if (authStore.pendingMfaChallengeId) {
        const response = await authStore.verifyTotp(form.mfaCode);
        if (response.status === "AUTHENTICATED") {
          await redirectAfterLogin();
        }
      } else {
        const response = await authStore.login(form.username, form.password);
        if (response.status !== "MFA_REQUIRED") {
          await redirectAfterLogin();
        }
      }
    } catch (error) {
      console.error("Login failed", error);
      errorMessage.value = t("login.loginFailed");
    } finally {
      isSubmitting.value = false;
    }
  }

  async function redirectAfterLogin() {
    await tenantStore.initialize();
    const hasPlatformPermission = authStore.canManagePlatform;
    router.push({
      path: hasPlatformPermission ? "/platform/tenants" : "/app/dashboard"
    });
  }

  return {
    authStore,
    errorMessage,
    form,
    isSubmitting,
    submit
  };
}
