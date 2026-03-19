import { flushPromises, mount } from "@vue/test-utils";
import SecurityView from ".";
import { authStore } from "../../stores/auth";
import { setLocale } from "../../i18n";

describe("SecurityView", () => {
  beforeEach(() => {
    setLocale("en");
  });

  it("starts TOTP enrollment and activates it", async () => {
    authStore.token = "token-123";
    authStore.user = {
      id: "user-admin",
      email: "admin@lombardio.local",
      displayName: "System Admin",
      mfaEnabled: false
    };

    vi.spyOn(authStore, "beginTotpEnrollment").mockResolvedValue({
      secret: "ABCDEF123456",
      otpauthUri: "otpauth://totp/Lombardio:admin@lombardio.local?secret=ABCDEF123456&issuer=Lombardio"
    });
    vi.spyOn(authStore, "activateTotp").mockResolvedValue({
      ...authStore.user,
      mfaEnabled: true,
      mfaMethods: ["TOTP"]
    });

    const wrapper = mount(SecurityView);

    await wrapper.find("button").trigger("click");
    await flushPromises();

    expect(wrapper.text()).toContain("ABCDEF123456");

    await wrapper.find('input[inputmode="numeric"]').setValue("123456");
    await wrapper.findAll("button")[1].trigger("click");
    await flushPromises();

    expect(authStore.activateTotp).toHaveBeenCalledWith("123456");
    expect(wrapper.text()).toContain("Two-factor authentication enabled");
  });

  it("updates the app language in settings", async () => {
    authStore.user = {
      id: "user-admin",
      email: "admin@lombardio.local",
      displayName: "System Admin",
      mfaEnabled: false
    };

    const wrapper = mount(SecurityView);

    await wrapper.find("select").setValue("de");
    await flushPromises();

    expect(wrapper.text()).toContain("Sprache gespeichert");
  });
});
