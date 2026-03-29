import { flushPromises, mount } from "@vue/test-utils";
import CustomerDetailView from ".";
import { setLocale } from "../../i18n";
import router from "../../router";
import { useAuthStore } from "../../stores/auth";
import { useTenantStore } from "../../stores/tenant";
import * as customerApi from "../../services/api/customer";
import * as kycApi from "../../services/api/kyc";
import * as amlApi from "../../services/api/aml";
import * as originationApi from "../../services/api/origination";

describe("CustomerDetailView", () => {
  let authStore;
  let tenantStore;

  beforeEach(() => {
    setLocale("de");
    vi.restoreAllMocks();
    authStore = useAuthStore();
    tenantStore = useTenantStore();
  });

  it("loads and saves aml data for the customer file", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    tenantStore.features = [{ tenantId: "tenant-default", featureKey: "aml-compliance", enabled: true }];
    authStore.token = "token-123";

    vi.spyOn(customerApi, "fetchCustomer").mockResolvedValue({
      id: "customer-1",
      customerNumber: "KD-1001",
      firstName: "Anna",
      lastName: "Becker",
      birthDate: "1988-04-12",
      phone: "+49 170 111111",
      email: "anna@example.test",
      wantsDigitalPawnTicket: true,
      onlineAccessStatus: "INVITED",
      street: "Hauptstrasse 1",
      postalCode: "10115",
      city: "Berlin"
    });
    vi.spyOn(originationApi, "fetchLoans").mockResolvedValue([
      {
        id: "loan-1",
        customer: {
          id: "customer-1",
          customerNumber: "KD-1001",
          displayName: "Anna Becker",
          birthDate: "1988-04-12",
          phone: "+49 170 111111",
          checkedDocumentType: "PERSONALAUSWEIS"
        },
        pledgeRecord: {
          recordedAt: "2026-03-18T12:00:00Z",
          languageCode: "de",
          retentionUntil: "2030-03-18",
          checkedDocumentType: "PERSONALAUSWEIS",
          powerOfAttorneyRequired: false
        },
        positions: [],
        pawnTickets: [
          {
            ticketNumber: "PS-1001",
            totalLoanValue: 200,
            dueDate: "2026-06-18"
          }
        ]
      }
    ]);
    vi.spyOn(kycApi, "fetchKycStatus").mockResolvedValue({
      status: "APPROVED",
      verificationMode: "MANUAL",
      verifiedUntil: "2030-03-18",
      documentType: "PERSONALAUSWEIS",
      documentNumber: "L01X00T47",
      documentValidUntil: "2030-03-18",
      documentFrontImageDataUrl: "data:image/png;base64,front",
      documentBackImageDataUrl: "data:image/png;base64,back",
      decisionNote: "Freigegeben"
    });
    vi.spyOn(amlApi, "fetchAmlStatus").mockResolvedValue({
      status: "REVIEW_REQUIRED",
      riskLevel: "HIGH",
      pepFlag: true,
      sanctionsHit: false,
      unusualTransactionFlag: true,
      sourceOfFundsChecked: false,
      suspiciousActivityReported: false,
      goamlReference: null,
      decisionNote: "EDD offen",
      lastScreenedAt: "2026-03-18T09:00:00",
      reviewedAt: "2026-03-18T10:00:00",
      featureAvailable: true,
      originationAllowed: false,
      decisionReason: "AML review required before loan origination"
    });
    const updateAmlSpy = vi.spyOn(amlApi, "updateAmlStatus").mockResolvedValue({
      status: "CLEAR",
      riskLevel: "MEDIUM",
      pepFlag: false,
      sanctionsHit: false,
      unusualTransactionFlag: false,
      sourceOfFundsChecked: true,
      suspiciousActivityReported: false,
      goamlReference: null,
      decisionNote: "Freigegeben",
      lastScreenedAt: "2026-03-18T09:00:00",
      reviewedAt: "2026-03-18T10:15:00",
      featureAvailable: true,
      originationAllowed: true,
      decisionReason: "AML review cleared for origination"
    });

    await router.push("/app/customers/customer-1");
    await router.isReady();

    const wrapper = mount(CustomerDetailView, {
      global: {
        plugins: [router]
      }
    });
    await flushPromises();

    expect(wrapper.text()).toContain("AML und GwG");
    expect(wrapper.text()).toContain("AML review required before loan origination");
    expect(wrapper.text()).toContain("Pfandverträge und Beleihungen");
    expect(wrapper.text()).toContain("PS-1001");
    expect(wrapper.text()).toContain("Online-Zugangsstatus: INVITED");

    wrapper.vm.aml.status = "CLEAR";
    wrapper.vm.aml.riskLevel = "MEDIUM";
    wrapper.vm.aml.sourceOfFundsChecked = true;
    wrapper.vm.aml.unusualTransactionFlag = false;
    wrapper.vm.aml.pepFlag = false;
    wrapper.vm.aml.decisionNote = "Freigegeben";

    await wrapper.vm.saveAml();
    await flushPromises();

    expect(updateAmlSpy).toHaveBeenCalledWith(
      "tenant-default",
      "customer-1",
      expect.objectContaining({
        status: "CLEAR",
        riskLevel: "MEDIUM",
        sourceOfFundsChecked: true
      }),
      "token-123"
    );
    expect(wrapper.text()).toContain("AML review cleared for origination");
  });
});
