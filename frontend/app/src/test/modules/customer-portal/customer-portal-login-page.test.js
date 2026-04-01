import { flushPromises, mount } from "@vue/test-utils";
import CustomerPortalLoginView from "../../../modules/customer-portal/ui/pages/customer-portal-login-page";
import { setLocale } from "../../../app/i18n";
import { useCustomerPortalStore } from "../../../app/session/state";

const push = vi.fn();

vi.mock("vue-router", () => ({
  useRouter: () => ({
    push
  })
}));

describe("CustomerPortalLoginView", () => {
  let customerPortalStore;

  beforeEach(() => {
    setLocale("de");
    push.mockReset();
    customerPortalStore = useCustomerPortalStore();
  });

  it("logs the customer in and redirects to the portal home", async () => {
    vi.spyOn(customerPortalStore, "login").mockResolvedValue({});

    const wrapper = mount(CustomerPortalLoginView);
    await wrapper.find('input[type="email"]').setValue("anna@example.test");
    await wrapper.find('input[type="password"]').setValue("secret123");
    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(customerPortalStore.login).toHaveBeenCalledWith("anna@example.test", "secret123");
    expect(push).toHaveBeenCalledWith({ name: "customer-portal-home" });
  });
});
