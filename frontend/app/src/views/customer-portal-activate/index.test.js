import { flushPromises, mount } from "@vue/test-utils";
import CustomerPortalActivateView from ".";
import { setLocale } from "../../i18n";
import { customerPortalStore } from "../../stores/customerPortal";
import * as customerPortalApi from "../../services/api/customerPortal";

const push = vi.fn();

vi.mock("vue-router", () => ({
  useRouter: () => ({
    push
  }),
  useRoute: () => ({
    params: {
      token: "invite-123"
    }
  })
}));

describe("CustomerPortalActivateView", () => {
  beforeEach(() => {
    setLocale("de");
    push.mockReset();
  });

  it("loads the invitation and activates the access", async () => {
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
