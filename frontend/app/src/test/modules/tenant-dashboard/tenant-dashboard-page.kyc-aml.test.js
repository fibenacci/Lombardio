import { flushPromises, mount } from "@vue/test-utils";
import TenantHomeView from "../../../modules/tenant-dashboard/ui/pages/tenant-dashboard-page";
import { setLocale } from "../../../app/i18n";
import { useTenantStore } from "../../../app/tenant-context/state";
import { useAuthStore } from "../../../app/session/state";
import * as originationApi from "../../../modules/loans/infrastructure/api/origination.api";
import * as customerApi from "../../../modules/customers/infrastructure/api/customer.api";
import * as amlApi from "../../../modules/customers/infrastructure/api/aml.api";
import * as kycApi from "../../../modules/customers/infrastructure/api/kyc.api";
import * as pawnTicketApi from "../../../modules/pawn-tickets/infrastructure/api/pawn-ticket.api";
import * as reportingApi from "../../../modules/tenant-dashboard/infrastructure/api/reporting.api";
import router from "../../../app/router";

function mountView() {
  return mount(TenantHomeView, {
    global: {
      plugins: [router]
    }
  });
}

describe("TenantHomeView - KYC & AML", () => {
  let tenantStore;

  beforeEach(() => {
    setLocale("de");
    vi.restoreAllMocks();
    useAuthStore();
    tenantStore = useTenantStore();
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    tenantStore.features = [];
    vi.spyOn(reportingApi, "fetchDashboardOverview").mockResolvedValue({ finance: {}, financeTrend: [], inventoryByCategory: [], transactionMix: [] });
    vi.spyOn(originationApi, "fetchValuationGuidelines").mockResolvedValue([]);
    vi.spyOn(pawnTicketApi, "fetchPawnTicketQuote").mockResolvedValue({});
  });

  it("allows approving kyc for a selected customer before origination", async () => {
    vi.spyOn(customerApi, "searchCustomers").mockResolvedValue([{ id: "customer-1", kycApproved: false }]);
    vi.spyOn(amlApi, "fetchAmlStatus").mockResolvedValue({ status: "REVIEW_REQUIRED", originationAllowed: false });
    vi.spyOn(kycApi, "fetchKycStatus").mockResolvedValue({ status: "IN_PROGRESS" });
    const updateKycSpy = vi.spyOn(kycApi, "updateKycStatus").mockResolvedValue({ status: "APPROVED" });

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.customers = [{ 
      id: "customer-1", 
      displayName: "Max", 
      kycApproved: false,
      documentFrontImageDataUrl: "data:front",
      documentBackImageDataUrl: "data:back"
    }];
    wrapper.vm.handleCustomerSelection({ value: { value: "customer-1" } });
    await flushPromises();

    const approveButton = wrapper.findAll("button").find((button) => button.text().includes("KYC manuell freigeben"));
    await approveButton.trigger("click");
    await flushPromises();

    expect(updateKycSpy).toHaveBeenCalled();
    expect(wrapper.vm.selectedCustomer.kycApproved).toBe(true);
  });

  it("shows optional provider verification action when tenant feature is enabled", async () => {
    tenantStore.features = [{ featureKey: "kyc-provider-verification", enabled: true }];

    vi.spyOn(kycApi, "fetchKycStatus").mockResolvedValue({ status: "NOT_STARTED" });
    const updateKycSpy = vi.spyOn(kycApi, "updateKycStatus").mockResolvedValue({ verificationMode: "PROVIDER", providerStatus: "PENDING" });

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.customers = [{ id: "customer-2", kycApproved: false }];
    wrapper.vm.handleCustomerSelection({ value: { value: "customer-2" } });
    await flushPromises();

    const providerButton = wrapper.findAll("button").find((button) => button.text().includes("Provider-Prüfung vormerken"));
    await providerButton.trigger("click");
    await flushPromises();

    expect(updateKycSpy).toHaveBeenCalledWith(expect.anything(), expect.anything(), expect.objectContaining({ verificationMode: "PROVIDER" }));
  });

  it("requires identity document data for new customers and stores manual kyc before origination", async () => {
    tenantStore.features = [{ featureKey: "aml-compliance", enabled: true }];

    vi.spyOn(customerApi, "createCustomer").mockResolvedValue({ id: "customer-new" });
    const updateKycSpy = vi.spyOn(kycApi, "updateKycStatus").mockResolvedValue({ status: "APPROVED" });
    const updateAmlSpy = vi.spyOn(amlApi, "updateAmlStatus").mockResolvedValue({ status: "CLEAR", originationAllowed: true });
    vi.spyOn(pawnTicketApi, "fetchPawnTicketQuote").mockResolvedValue({});
    vi.spyOn(originationApi, "createLoan").mockResolvedValue({ id: "loan-1", pawnTickets: [] });

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.useNewCustomer = true;
    Object.assign(wrapper.vm.newCustomer, { 
      customerNumber: "KD-1",
      firstName: "Lena", 
      lastName: "Sommer",
      birthDate: "1990-01-01",
      phone: "12345"
    });
    Object.assign(wrapper.vm.newCustomerKyc, { 
      documentType: "PERSONALAUSWEIS", 
      documentNumber: "D-1",
      documentValidUntil: "2030-01-01",
      documentFrontImageDataUrl: "data:front",
      documentBackImageDataUrl: "data:back"
    });
    Object.assign(wrapper.vm.newCustomerAml, {
      status: "CLEAR",
      riskLevel: "LOW"
    });
    wrapper.vm.positions = [{ 
      ticketGroup: 1, 
      label: "Gold", 
      description: "Desc",
      guidelineId: "g1", 
      pledgedValue: 200 
    }];

    await wrapper.vm.submitLoan();
    await flushPromises();

    expect(updateKycSpy).toHaveBeenCalled();
    expect(updateAmlSpy).toHaveBeenCalled();
  });

  it("can prefill new customer identity document data via OCR feature", async () => {
    tenantStore.features = [{ featureKey: "kyc-document-ocr", enabled: true }];

    const prefillSpy = vi.spyOn(kycApi, "prefillKycDocument").mockResolvedValue({
      available: true,
      matched: true,
      documentNumber: "XK1234567",
      documentValidUntil: "2031-03-18"
    });

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.useNewCustomer = true;
    wrapper.vm.newCustomerKyc.documentFrontImageDataUrl = "data:front";

    await wrapper.vm.prefillNewCustomerDocumentData();
    await flushPromises();

    expect(prefillSpy).toHaveBeenCalled();
    expect(wrapper.vm.newCustomerKyc.documentNumber).toBe("XK1234567");
  });

  it("blocks new-customer loan submission when manual kyc documents are missing", async () => {
    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.useNewCustomer = true;
    wrapper.vm.positions = [{ ticketGroup: 1, label: "Gold", guidelineId: "g1", pledgedValue: 180 }];

    await wrapper.vm.submitLoan();
    await flushPromises();

    expect(wrapper.vm.errorMessage).toBe("Bitte Vorder- und Rückseite des Dokuments zuerst hinterlegen.");
  });
});
