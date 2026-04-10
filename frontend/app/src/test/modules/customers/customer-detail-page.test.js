import { flushPromises, mount } from "@vue/test-utils";
import CustomerDetailView from "../../../modules/customers/ui/pages/customer-detail-page";
import { setLocale } from "../../../app/i18n";
import router from "../../../app/router";
import { useTenantStore } from "../../../app/tenant-context/state";
import * as customerApi from "../../../modules/customers/infrastructure/api/customer.api";
import * as kycApi from "../../../modules/customers/infrastructure/api/kyc.api";
import * as amlApi from "../../../modules/customers/infrastructure/api/aml.api";
import * as originationApi from "../../../modules/loans/infrastructure/api/origination.api";
import { KycStatus, AmlStatus, AmlRiskLevel } from "../../../modules/customers/domain/model/customer";

describe("CustomerDetailView", () => {
  let tenantStore;

  beforeEach(() => {
    setLocale("de");
    vi.restoreAllMocks();
    tenantStore = useTenantStore();
  });

  it("loads and saves aml data for the customer file", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    tenantStore.features = [{ tenantId: "tenant-default", featureKey: "aml-compliance", enabled: true }];
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
      city: "Berlin",
      displayName: "Anna Becker",
      kycStatus: "APPROVED",
      kycApproved: true,
      kycDocumentType: "PERSONALAUSWEIS"
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
          id: "pledge-1",
          recordedAt: "2026-03-18T12:00:00Z",
          languageCode: "de",
          retentionUntil: "2030-03-18",
          pledgorName: "Anna Becker",
          pledgorStreet: "Hauptstrasse 1",
          pledgorPostalCode: "10115",
          pledgorCity: "Berlin",
          pledgorBirthDate: "1988-04-12",
          checkedDocumentType: "PERSONALAUSWEIS",
          powerOfAttorneyRequired: false,
          bearerName: "",
          bearerStreet: "",
          bearerPostalCode: "",
          bearerCity: ""
        },
        positions: [],
        pawnTickets: [
          {
            contractNumber: "C-1001",
            contractBarcode: "12345",
            ticketNumber: "PS-1001",
            termsVersion: "1.0",
            termsAndConditionsText: "...",
            createdAt: "2026-03-18T12:00:00Z",
            dueDate: "2026-06-18",
            earliestAuctionDate: "2026-07-18",
            termMonths: 3,
            totalLoanValue: 200,
            monthlyInterestRate: 1.0,
            monthlyOperatingFee: 2.0,
            manualMonthlyOperatingFeeRequired: false,
            totalInterestAmount: 6.0,
            totalOperatingFeeAmount: 12.0,
            totalRepaymentAmount: 218.0,
            legalText: "...",
            positions: []
          }
        ]
      }
    ]);
    vi.spyOn(kycApi, "fetchKycStatus").mockResolvedValue({
      customerId: "customer-1",
      status: KycStatus.APPROVED,
      verificationMode: "MANUAL",
      verifiedUntil: "2030-03-18",
      documentType: "PERSONALAUSWEIS",
      documentNumber: "L01X00T47",
      documentValidUntil: "2030-03-18",
      decisionNote: "Freigegeben",
      providerName: "",
      providerReference: "",
      providerStatus: "",
      providerVerificationAvailable: false,
      confidence: 1.0
    });
    vi.spyOn(kycApi, "fetchKycDocuments").mockResolvedValue({
      customerId: "customer-1",
      documentFrontImageDataUrl: "data:image/png;base64,front",
      documentBackImageDataUrl: "data:image/png;base64,back"
    });
    vi.spyOn(amlApi, "fetchAmlStatus").mockResolvedValue({
      status: AmlStatus.REVIEW_REQUIRED,
      riskLevel: AmlRiskLevel.HIGH,
      pepFlag: true,
      sanctionsHit: false,
      unusualTransactionFlag: true,
      sourceOfFundsChecked: false,
      suspiciousActivityReported: false,
      goamlReference: "",
      decisionNote: "EDD offen",
      lastScreenedAt: "2026-03-18T09:00:00",
      reviewedAt: "2026-03-18T10:00:00",
      featureAvailable: true,
      originationAllowed: false,
      decisionReason: "AML review required before loan origination",
      customerId: "customer-1"
    });
    const updateAmlSpy = vi.spyOn(amlApi, "updateAmlStatus").mockResolvedValue({
      status: AmlStatus.CLEAR,
      riskLevel: AmlRiskLevel.MEDIUM,
      pepFlag: false,
      sanctionsHit: false,
      unusualTransactionFlag: false,
      sourceOfFundsChecked: true,
      suspiciousActivityReported: false,
      goamlReference: "",
      decisionNote: "Freigegeben",
      lastScreenedAt: "2026-03-18T09:00:00",
      reviewedAt: "2026-03-18T10:15:00",
      featureAvailable: true,
      originationAllowed: true,
      decisionReason: "AML review cleared for origination",
      customerId: "customer-1"
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

    wrapper.vm.aml.status = AmlStatus.CLEAR;
    wrapper.vm.aml.riskLevel = AmlRiskLevel.MEDIUM;
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
        status: AmlStatus.CLEAR,
        riskLevel: AmlRiskLevel.MEDIUM,
        sourceOfFundsChecked: true
      })
    );
    expect(wrapper.text()).toContain("AML review cleared for origination");
  });

  it("blocks manual kyc save when document images are missing", async () => {
    tenantStore.selectedTenantId = "tenant-default";
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
      city: "Berlin",
      displayName: "Anna Becker",
      kycStatus: "NOT_STARTED",
      kycApproved: false,
      kycDocumentType: null
    });
    vi.spyOn(originationApi, "fetchLoans").mockResolvedValue([]);
    vi.spyOn(kycApi, "fetchKycStatus").mockResolvedValue({
      customerId: "customer-1",
      status: KycStatus.NOT_STARTED,
      verificationMode: "MANUAL",
      verifiedUntil: "",
      documentType: "",
      documentNumber: "",
      documentValidUntil: "",
      decisionNote: "",
      providerName: "",
      providerReference: "",
      providerStatus: "",
      providerVerificationAvailable: false,
      confidence: 0
    });
    vi.spyOn(kycApi, "fetchKycDocuments").mockResolvedValue({
      customerId: "customer-1",
      documentFrontImageDataUrl: "",
      documentBackImageDataUrl: ""
    });

    await router.push("/app/customers/customer-1");
    await router.isReady();

    const wrapper = mount(CustomerDetailView, {
      global: {
        plugins: [router]
      }
    });
    await flushPromises();

    wrapper.vm.kyc.status = KycStatus.APPROVED;
    await wrapper.vm.saveKyc();
    await flushPromises();

    expect(wrapper.text()).toContain("Bitte Vorder- und Rückseite des Dokuments zuerst hinterlegen.");
  });
});
