import { flushPromises, mount } from "@vue/test-utils";
import LoginView from ".";
import { setLocale } from "../../i18n";
import { authStore } from "../../stores/auth";

const push = vi.fn();

vi.mock("vue-router", () => ({
  useRouter: () => ({
    push
  })
}));

describe("LoginView", () => {
  beforeEach(() => {
    setLocale("en");
    push.mockReset();
  });

  it("submits credentials and redirects platform admins to the platform area", async () => {
    vi.spyOn(authStore, "login").mockResolvedValue();
    vi.spyOn(authStore, "canManagePlatform").mockReturnValue(true);

    const wrapper = mount(LoginView);

    await wrapper.find('input[type="email"]').setValue("admin@lombardio.local");
    await wrapper.find('input[type="password"]').setValue("change-me");
    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(authStore.login).toHaveBeenCalledWith("admin@lombardio.local", "change-me");
    expect(push).toHaveBeenCalledWith({ path: "/platform/tenants" });
  });

  it("redirects tenant users into the tenant app after login", async () => {
    vi.spyOn(authStore, "login").mockResolvedValue();
    vi.spyOn(authStore, "canManagePlatform").mockReturnValue(false);

    const wrapper = mount(LoginView);

    await wrapper.find('input[type="email"]').setValue("admin@lombardio.local");
    await wrapper.find('input[type="password"]').setValue("change-me");
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
      return { status: "AUTHENTICATED" };
    });
    vi.spyOn(authStore, "canManagePlatform").mockReturnValue(false);

    const wrapper = mount(LoginView);

    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(push).not.toHaveBeenCalled();
    expect(wrapper.text()).toContain("Verify code");

    await wrapper.find('input[inputmode="numeric"]').setValue("123456");
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

    expect(wrapper.text()).toContain("Invalid credentials");
  });
});
