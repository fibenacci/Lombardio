import { flushPromises, mount } from "@vue/test-utils";
import LoginView from "../../../app/session/ui/pages/login-page";
import { setLocale } from "../../../app/i18n";
import { useAuthStore } from "../../../app/session/state";
import { useTenantStore } from "../../../app/tenant-context/state";

const push = vi.fn();

vi.mock("vue-router", () => ({
  useRouter: () => ({
    push
  })
}));

describe("LoginView", () => {
  let authStore;
  let tenantStore;

  beforeEach(() => {
    setLocale("en");
    push.mockReset();
    authStore = useAuthStore();
    tenantStore = useTenantStore();
    vi.spyOn(tenantStore, "initialize").mockResolvedValue();
  });

  it("submits credentials and redirects platform admins to the platform area", async () => {
    vi.spyOn(authStore, "login").mockImplementation(async () => {
      authStore.user = { permissions: ["platform.tenants.read"] };
      return { status: "AUTHENTICATED" };
    });

    const wrapper = mount(LoginView);

    await wrapper.find("#username").setValue("admin@lombardio.local");
    await wrapper.find("#password").find("input").setValue("change-me");
    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(authStore.login).toHaveBeenCalledWith("admin@lombardio.local", "change-me");
    expect(push).toHaveBeenCalledWith({ path: "/platform/tenants" });
  });

  it("redirects tenant users into the tenant app after login", async () => {
    vi.spyOn(authStore, "login").mockImplementation(async () => {
      authStore.user = { permissions: ["users.read"] };
      return { status: "AUTHENTICATED" };
    });

    const wrapper = mount(LoginView);

    await wrapper.find("#username").setValue("admin@lombardio.local");
    await wrapper.find("#password").find("input").setValue("change-me");
    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(push).toHaveBeenCalledWith({ path: "/app/dashboard" });
  });

  it("shows the MFA step and does not redirect before code verification", async () => {
    vi.spyOn(authStore, "login").mockImplementation(async () => {
      authStore.pendingMfaChallengeId = "challenge-123";
      return { status: "MFA_REQUIRED", challengeId: "challenge-123", mfaMethods: ["TOTP"] };
    });
    vi.spyOn(authStore, "verifyTotp").mockImplementation(async () => {
      authStore.pendingMfaChallengeId = "";
      authStore.user = { permissions: ["users.read"] };
      return { status: "AUTHENTICATED" };
    });

    const wrapper = mount(LoginView);

    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(push).not.toHaveBeenCalled();
    expect(wrapper.text()).toContain("Verify code");

    await wrapper.find("#mfaCode").setValue("123456");
    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(authStore.verifyTotp).toHaveBeenCalledWith("123456");
    expect(push).toHaveBeenCalledWith({ path: "/app/dashboard" });
  });

  it("renders backend error messages on failed login", async () => {
    vi.spyOn(authStore, "login").mockRejectedValue(new Error("Invalid credentials"));

    const wrapper = mount(LoginView);

    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(wrapper.text()).toContain("Login failed");
  });
});
