import { flushPromises, mount } from "@vue/test-utils";
import CustomerPortalActivateView from "../../../modules/customer-portal/ui/pages/customer-portal-activate-page";
import { setLocale } from "../../../app/i18n";
import { useCustomerPortalStore } from "../../../app/session/state";
import * as customerPortalApi from "../../../modules/customer-portal/infrastructure/api/customer-portal.api";

const push = vi.fn();

vi.mock("vue-router", () => ({
  useRouter: () => ({
    push
  }),
  useRoute: () => ({
    params: {}
  })
}));

describe("CustomerPortalActivateView", () => {
  let customerPortalStore;

  beforeEach(() => {
    setLocale("de");
    push.mockReset();
    customerPortalStore = useCustomerPortalStore();
  });

  it("loads the invitation and activates the access", async () => {
    window.location.hash = "#invite-123";
    vi.spyOn(customerPortalApi, "fetchPortalInvitation").mockResolvedValue({
      customerDisplayName: "Anna Becker",
      email: "anna@example.test",
      status: "INVITED"
    });
    vi.spyOn(customerPortalStore, "acceptInvitation").mockResolvedValue({});

    const wrapper = mount(CustomerPortalActivateView);
    await flushPromises();

    expect(wrapper.text()).toContain("Anna Becker");

    await wrapper.findAll('input[type="password"]')[0].setValue("secret123");
    await wrapper.findAll('input[type="password"]')[1].setValue("secret123");
    await wrapper.find("form").trigger("submit.prevent");
    await flushPromises();

    expect(customerPortalStore.acceptInvitation).toHaveBeenCalledWith("invite-123", "secret123");
    expect(push).toHaveBeenCalledWith({ name: "customer-portal-home" });
  });
});
