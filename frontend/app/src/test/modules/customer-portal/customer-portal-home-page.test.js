import { flushPromises, mount } from "@vue/test-utils";
import CustomerPortalHomeView from "../../../modules/customer-portal/ui/pages/customer-portal-home-page";
import { setLocale } from "../../../app/i18n";
import { useCustomerPortalStore } from "../../../app/session/state";
import * as customerPortalApi from "../../../modules/customer-portal/infrastructure/api/customer-portal.api";

const push = vi.fn();

vi.mock("vue-router", () => ({
  useRouter: () => ({
    push
  })
}));

describe("CustomerPortalHomeView", () => {
  let customerPortalStore;

  beforeEach(() => {
    customerPortalStore = useCustomerPortalStore();
    setLocale("de");
    push.mockReset();
    customerPortalStore.customer = {
      customerId: "customer-1",
      displayName: "Anna Becker"
    };
  });

  it("loads portal pawn tickets and can trigger a document download", async () => {
    vi.spyOn(customerPortalApi, "fetchCustomerPortalPawnTickets").mockResolvedValue([
      {
        contractNumber: "PS-1001",
        ticketNumber: "PS-1001",
        dueDate: "2026-06-18",
        loanAmount: 200,
        totalRepaymentAmount: 219.5,
        positionCount: 1
      }
    ]);
    vi.spyOn(customerPortalApi, "fetchCustomerPortalDocument").mockResolvedValue(new Blob(["pdf"], { type: "application/pdf" }));
    const openSpy = vi.spyOn(window, "open").mockReturnValue(null);
    const objectUrlSpy = vi.fn().mockReturnValue("blob:portal");
    URL.createObjectURL = objectUrlSpy;

    const wrapper = mount(CustomerPortalHomeView);
    await flushPromises();

    expect(wrapper.text()).toContain("PS-1001");

    await wrapper.find("button:not(.portal-logout)").trigger("click");
    await flushPromises();

    expect(customerPortalApi.fetchCustomerPortalDocument).toHaveBeenCalledWith("PS-1001");
    expect(openSpy).toHaveBeenCalled();
  });
});
