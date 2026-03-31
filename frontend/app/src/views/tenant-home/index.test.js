import { flushPromises, mount } from "@vue/test-utils";
import TenantHomeView from ".";
import { setLocale } from "../../i18n";
import { useTenantStore } from "../../stores/tenant";
import { useAuthStore } from "../../stores/auth";
import * as originationApi from "../../services/api/origination";
import * as customerApi from "../../services/api/customer";
import * as amlApi from "../../services/api/aml";
import * as kycApi from "../../services/api/kyc";
import * as pawnTicketApi from "../../services/api/pawnTicket";
import * as reportingApi from "../../services/api/reporting";
import router from "../../router";

function mountView() {
  return mount(TenantHomeView, {
    global: {
      plugins: [router]
    }
  });
}

describe("TenantHomeView", () => {
  let authStore;
  let tenantStore;

  beforeEach(() => {
    setLocale("de");
    vi.restoreAllMocks();
    authStore = useAuthStore();
    tenantStore = useTenantStore();
    tenantStore.features = [];
    vi.spyOn(amlApi, "fetchAmlStatus").mockResolvedValue({
      customerId: "customer-berlin-1",
      status: "CLEAR",
      riskLevel: "LOW",
      pepFlag: false,
      sanctionsHit: false,
      unusualTransactionFlag: false,
      sourceOfFundsChecked: true,
      suspiciousActivityReported: false,
      goamlReference: null,
      decisionReason: "AML review cleared for origination",
      featureAvailable: true,
      originationAllowed: true
    });
    vi.spyOn(amlApi, "assessAmlOrigination").mockResolvedValue({
      customerId: "customer-berlin-1",
      status: "CLEAR",
      riskLevel: "LOW",
      pepFlag: false,
      sanctionsHit: false,
      unusualTransactionFlag: false,
      sourceOfFundsChecked: true,
      suspiciousActivityReported: false,
      goamlReference: null,
      decisionReason: "AML review cleared for origination",
      featureAvailable: true,
      originationAllowed: true
    });
    vi.spyOn(kycApi, "fetchKycStatus").mockResolvedValue({
      customerId: "customer-berlin-1",
      verificationMode: "MANUAL",
      status: "APPROVED",
      verifiedUntil: "2027-03-18",
      documentType: "PERSONALAUSWEIS",
      documentNumber: "L01X00T47",
      documentValidUntil: "2030-03-18",
      documentFrontImageDataUrl: "data:image/png;base64,front",
      documentBackImageDataUrl: "data:image/png;base64,back",
      decisionNote: "Freigegeben"
    });
    vi.spyOn(reportingApi, "fetchDashboardOverview").mockResolvedValue({
      rangeStart: "2026-03-05",
      rangeEnd: "2026-03-18",
      generatedAt: "2026-03-18T12:00:00Z",
      finance: {
        cashInflow: 500.8,
        cashOutflow: 440,
        netCashflow: 60.8,
        realizedRevenue: 40.8,
        activeLoanExposure: 440,
        activeTicketCount: 2,
        averageTicketValue: 220
      },
      financeTrend: [
        { date: "2026-03-16", cashInflow: 219.5, cashOutflow: 0, realizedRevenue: 19.5 },
        { date: "2026-03-17", cashInflow: 281.3, cashOutflow: 440, realizedRevenue: 21.3 }
      ],
      inventoryByCategory: [
        { category: "Apple iPhone 14", itemCount: 1, pledgedValue: 260 },
        { category: "Goldring 585", itemCount: 1, pledgedValue: 180 }
      ],
      transactionMix: [
        { type: "EXTEND", transactionCount: 1, totalAmount: 281.3 },
        { type: "REDEEM", transactionCount: 1, totalAmount: 219.5 }
      ]
    });
  });

  it("blocks submit when required fields are missing", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    authStore.token = "token-123";

    vi.spyOn(originationApi, "fetchValuationGuidelines").mockResolvedValue([
      {
        id: "guideline-gold-585",
        label: "Goldring 585",
        description: "Gelbgold 14 Karat",
        baseLoanValue: 180
      },
      {
        id: "guideline-iphone-14",
        label: "Apple iPhone 14 128GB",
        description: "gebraucht, funktionsfaehig",
        baseLoanValue: 260
      }
    ]);
    vi.spyOn(customerApi, "searchCustomers").mockResolvedValue([]);
    vi.spyOn(kycApi, "updateKycStatus").mockResolvedValue({
      customerId: "customer-berlin-1",
      status: "APPROVED",
      verifiedUntil: "2027-03-18",
      documentType: "PERSONALAUSWEIS",
      decisionNote: "Freigegeben"
    });
    vi.spyOn(pawnTicketApi, "fetchPawnTicketQuote").mockResolvedValue({
      contractNumber: "QUOTE",
      contractBarcode: "QUOTE",
      termsVersion: "AGB-2026-03",
      termsAndConditionsText: "AGB",
      dueDate: "2026-06-18",
      earliestAuctionDate: "2026-07-18",
      monthlyInterestRate: 1,
      monthlyOperatingFee: 4.5,
      totalInterestAmount: 6,
      totalOperatingFeeAmount: 13.5,
      totalRepaymentAmount: 219.5,
      legalText: "Kostenmodell gemaess PfandlV.",
      positions: []
    });
    const createLoanSpy = vi.spyOn(originationApi, "createLoan").mockResolvedValue({});

    const wrapper = mountView();
    await flushPromises();

    const submitButton = wrapper.findAll("button").find((button) => button.text().includes("Beleihung abschließen"));
    expect(submitButton.attributes("disabled")).toBeDefined();
    expect(createLoanSpy).not.toHaveBeenCalled();
  });

  it("creates a loan and shows the pawn ticket preview", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    authStore.token = "token-123";

    vi.spyOn(originationApi, "fetchValuationGuidelines").mockResolvedValue([
      {
        id: "guideline-gold-585",
        label: "Goldring 585",
        description: "Gelbgold 14 Karat",
        baseLoanValue: 180
      }
    ]);
    vi.spyOn(customerApi, "searchCustomers").mockResolvedValue([
      {
        id: "customer-berlin-1",
        customerNumber: "KD-1001",
        displayName: "Anna Becker",
        birthDate: "1988-04-12",
        phone: "+49 170 111111",
        kycStatus: "APPROVED",
        kycApproved: true,
        checkedDocumentType: "PERSONALAUSWEIS"
      }
    ]);
    vi.spyOn(kycApi, "updateKycStatus").mockResolvedValue({
      customerId: "customer-berlin-1",
      status: "APPROVED",
      verifiedUntil: "2027-03-18",
      documentType: "PERSONALAUSWEIS",
      decisionNote: "Freigegeben"
    });
    vi.spyOn(pawnTicketApi, "fetchPawnTicketQuote").mockResolvedValue({
      contractNumber: "QUOTE",
      contractBarcode: "QUOTE",
      termsVersion: "AGB-2026-03",
      termsAndConditionsText: "AGB",
      dueDate: "2026-06-18",
      earliestAuctionDate: "2026-07-18",
      monthlyInterestRate: 1,
      monthlyOperatingFee: 4.5,
      totalInterestAmount: 6,
      totalOperatingFeeAmount: 13.5,
      totalRepaymentAmount: 219.5,
      legalText: "Kostenmodell gemaess PfandlV.",
      positions: []
    });
    vi.spyOn(pawnTicketApi, "fetchPawnTicketDocument").mockResolvedValue(new Blob(["pdf"], { type: "application/pdf" }));
    vi.spyOn(pawnTicketApi, "fetchPawnTicketLabels").mockResolvedValue(new Blob(["pdf"], { type: "application/pdf" }));
    const openSpy = vi.spyOn(window, "open").mockReturnValue({ addEventListener: vi.fn(), focus: vi.fn(), print: vi.fn() });
    const objectUrlSpy = vi.fn().mockReturnValue("blob:ticket");
    URL.createObjectURL = objectUrlSpy;
    vi.spyOn(originationApi, "createLoan").mockResolvedValue({
      id: "loan-1",
      customer: {
        id: "customer-berlin-1",
        customerNumber: "KD-1001",
        displayName: "Anna Becker",
        phone: "+49 170 111111"
      },
      positions: [
        {
          id: "position-1",
          ticketGroup: 1,
          label: "Goldring",
          description: "Ring mit Gravur",
          guidelineLabel: "Goldring 585",
          baseLoanValue: 180,
          pledgedValue: 200
        },
        {
          id: "position-2",
          ticketGroup: 2,
          label: "iPhone",
          description: "iPhone 14",
          guidelineLabel: "iPhone 14",
          baseLoanValue: 260,
          pledgedValue: 260
        }
      ],
      pawnTickets: [
        {
          contractNumber: "PS-1001",
          contractBarcode: "PS-1001",
          ticketNumber: "PS-1001",
          termsVersion: "AGB-2026-03",
          termsAndConditionsText: "AGB text",
          dueDate: "2026-06-16",
          earliestAuctionDate: "2026-07-16",
          termMonths: 3,
          totalLoanValue: 200,
          monthlyInterestRate: 1,
          monthlyOperatingFee: 4.5,
          manualMonthlyOperatingFeeRequired: false,
          totalInterestAmount: 6,
          totalOperatingFeeAmount: 13.5,
          totalRepaymentAmount: 219.5,
          legalText: "Pfandschein gemaess Pfandleihverordnung.",
          positions: [
            {
              itemNumber: "PS-1001-01",
              itemBarcode: "PS-1001-01",
              label: "Goldring",
              description: "Ring mit Gravur",
              pledgedValue: 200
            }
          ]
        },
        {
          contractNumber: "PS-1002",
          contractBarcode: "PS-1002",
          ticketNumber: "PS-1002",
          termsVersion: "AGB-2026-03",
          termsAndConditionsText: "AGB text",
          dueDate: "2026-06-16",
          earliestAuctionDate: "2026-07-16",
          termMonths: 3,
          totalLoanValue: 260,
          monthlyInterestRate: 1,
          monthlyOperatingFee: 4.5,
          manualMonthlyOperatingFeeRequired: false,
          totalInterestAmount: 7.8,
          totalOperatingFeeAmount: 13.5,
          totalRepaymentAmount: 281.3,
          legalText: "Pfandschein gemaess Pfandleihverordnung.",
          positions: [
            {
              itemNumber: "PS-1002-01",
              itemBarcode: "PS-1002-01",
              label: "iPhone",
              description: "iPhone 14",
              pledgedValue: 260
            }
          ]
        }
      ]
    });

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.selectedCustomerId = "customer-berlin-1";
    wrapper.vm.positions = [
      {
        ticketGroup: 1,
        label: "Goldring 585",
        description: "Gelbgold 14 Karat",
        guidelineId: "guideline-gold-585",
        pledgedValue: 200
      },
      {
        ticketGroup: 2,
        label: "Apple iPhone 14 128GB",
        description: "gebraucht, funktionsfaehig",
        guidelineId: "guideline-iphone-14",
        pledgedValue: 260
      }
    ];
    await wrapper.vm.submitLoan();
    await flushPromises();

    expect(originationApi.createLoan).toHaveBeenCalledWith(
      "tenant-default",
      {
        customerId: "customer-berlin-1",
        positions: [
          {
            ticketGroup: 1,
            label: "Goldring 585",
            description: "Gelbgold 14 Karat",
            guidelineId: "guideline-gold-585",
            pledgedValue: 200
          },
          {
            ticketGroup: 2,
            label: "Apple iPhone 14 128GB",
            description: "gebraucht, funktionsfaehig",
            guidelineId: "guideline-iphone-14",
            pledgedValue: 260
          }
        ],
        termMonths: 3,
        manualMonthlyOperatingFee: null,
        thirdPartyPledgorPresentation: false,
        bearerName: null,
        bearerStreet: null,
        bearerPostalCode: null,
        bearerCity: null,
        powerOfAttorneyDocumentDataUrl: null
      },
      "token-123"
    );
    expect(wrapper.text()).toContain("PS-1001");
    expect(wrapper.text()).toContain("Vertrag: PS-1001");
    expect(wrapper.text()).toContain("PS-1001-01");
    expect(wrapper.text()).toContain("PS-1002");
    expect(wrapper.text()).toContain("Anna Becker");
    expect(wrapper.text()).toContain("219.5 EUR");

    const pdfButton = wrapper.findAll("button").find((button) => button.text().includes("Pfandschein als PDF"));
    await pdfButton.trigger("click");
    await flushPromises();

    expect(pawnTicketApi.fetchPawnTicketDocument).toHaveBeenCalledWith("PS-1001", "token-123");
    expect(objectUrlSpy).toHaveBeenCalled();
    expect(openSpy).toHaveBeenCalledWith("blob:ticket", "_blank", "noopener,noreferrer");
  });

  it("shows structured field errors from the backend", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    authStore.token = "token-123";

    vi.spyOn(originationApi, "fetchValuationGuidelines").mockResolvedValue([
      {
        id: "guideline-gold-585",
        label: "Goldring 585",
        description: "Gelbgold 14 Karat",
        baseLoanValue: 180
      }
    ]);
    vi.spyOn(customerApi, "searchCustomers").mockResolvedValue([
      {
        id: "customer-berlin-1",
        customerNumber: "KD-1001",
        displayName: "Anna Becker",
        phone: "+49 170 111111",
        kycStatus: "APPROVED",
        kycApproved: true
      }
    ]);
    vi.spyOn(kycApi, "updateKycStatus").mockResolvedValue({
      customerId: "customer-berlin-1",
      status: "APPROVED",
      verifiedUntil: "2027-03-18",
      documentType: "PERSONALAUSWEIS",
      decisionNote: "Freigegeben"
    });
    vi.spyOn(pawnTicketApi, "fetchPawnTicketQuote").mockResolvedValue({
      contractNumber: "QUOTE",
      contractBarcode: "QUOTE",
      termsVersion: "AGB-2026-03",
      termsAndConditionsText: "AGB",
      dueDate: "2026-06-18",
      earliestAuctionDate: "2026-07-18",
      monthlyInterestRate: 1,
      monthlyOperatingFee: 4.5,
      totalInterestAmount: 6,
      totalOperatingFeeAmount: 13.5,
      totalRepaymentAmount: 219.5,
      legalText: "Kostenmodell gemaess PfandlV.",
      positions: []
    });
    const validationError = new Error("Validation failed");
    validationError.status = 400;
    validationError.fieldErrors = [
      { field: "positions[0].description", message: "must not be blank" },
      { field: "customerId", message: "must not be blank" }
    ];
    vi.spyOn(originationApi, "createLoan").mockRejectedValue(validationError);

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.selectedCustomerOption = wrapper.vm.customerOptions[0];
    wrapper.vm.handleCustomerSelection({ value: wrapper.vm.customerOptions[0] });
    await wrapper.findAll("select")[0].setValue("guideline-gold-585");
    await flushPromises();

    const textInputs = wrapper.findAll('input[type="text"]');
    await textInputs[textInputs.length - 1].setValue("");
    const numberInputs = wrapper.findAll('input[type="number"]');
    await numberInputs[0].setValue("200");
    const submitButton = wrapper.findAll("button").find((button) => button.text().includes("Beleihung abschließen"));
    await submitButton.trigger("click");
    await flushPromises();

    expect(wrapper.text()).toContain("Validation failed");
    expect(wrapper.text()).toContain("positions 0 description");
    expect(wrapper.text()).toContain("customer Id");
    expect(wrapper.text()).toContain("must not be blank");
  });

  it("renders reporting cards in the dashboard", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    authStore.token = "token-123";

    vi.spyOn(originationApi, "fetchValuationGuidelines").mockResolvedValue([]);
    vi.spyOn(customerApi, "searchCustomers").mockResolvedValue([]);

    const wrapper = mountView();
    await flushPromises();

    expect(wrapper.text()).toContain("Finanzen und Pfandbestand");
    expect(wrapper.text()).toContain("Realisierte Erträge");
    expect(wrapper.text()).toContain("Apple iPhone 14");
    expect(wrapper.text()).toContain("Verlängerung");
  });

  it("shows a separate quote preview per pawn-ticket group", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    authStore.token = "token-123";

    vi.spyOn(originationApi, "fetchValuationGuidelines").mockResolvedValue([
      {
        id: "guideline-gold-585",
        label: "Goldring 585",
        description: "Gelbgold 14 Karat",
        baseLoanValue: 180
      },
      {
        id: "guideline-iphone-14",
        label: "Apple iPhone 14 128GB",
        description: "gebraucht, funktionsfaehig",
        baseLoanValue: 260
      }
    ]);
    vi.spyOn(customerApi, "searchCustomers").mockResolvedValue([]);
    vi.spyOn(kycApi, "updateKycStatus").mockResolvedValue({
      customerId: "customer-berlin-1",
      status: "APPROVED",
      verifiedUntil: "2027-03-18",
      documentType: "PERSONALAUSWEIS",
      decisionNote: "Freigegeben"
    });
    vi.spyOn(pawnTicketApi, "fetchPawnTicketQuote")
      .mockResolvedValueOnce({
        contractNumber: "PS-1",
        contractBarcode: "PS-1",
        termsVersion: "AGB-2026-03",
        termsAndConditionsText: "AGB",
        dueDate: "2026-06-18",
        earliestAuctionDate: "2026-07-18",
        monthlyInterestRate: 1,
        monthlyOperatingFee: 4.5,
        totalInterestAmount: 6,
        totalOperatingFeeAmount: 13.5,
        totalRepaymentAmount: 219.5,
        legalText: "Kostenmodell Pfandschein 1.",
        positions: []
      })
      .mockResolvedValueOnce({
        contractNumber: "PS-2",
        contractBarcode: "PS-2",
        termsVersion: "AGB-2026-03",
        termsAndConditionsText: "AGB",
        dueDate: "2026-06-18",
        earliestAuctionDate: "2026-07-18",
        monthlyInterestRate: 1,
        monthlyOperatingFee: 5.5,
        totalInterestAmount: 7.8,
        totalOperatingFeeAmount: 16.5,
        totalRepaymentAmount: 284.3,
        legalText: "Kostenmodell Pfandschein 2.",
        positions: []
      });

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.positions = [
      {
        ticketGroup: 1,
        label: "Goldring 585",
        description: "Gelbgold 14 Karat",
        guidelineId: "guideline-gold-585",
        pledgedValue: 200
      },
      {
        ticketGroup: 2,
        label: "Apple iPhone 14 128GB",
        description: "gebraucht, funktionsfaehig",
        guidelineId: "guideline-iphone-14",
        pledgedValue: 260
      }
    ];
    await wrapper.vm.$nextTick();
    await flushPromises();

    expect(pawnTicketApi.fetchPawnTicketQuote).toHaveBeenCalledTimes(2);
    expect(wrapper.text()).toContain("Pfandschein 1");
    expect(wrapper.text()).toContain("Pfandschein 2");
    expect(wrapper.text()).toContain("Kostenmodell Pfandschein 1.");
    expect(wrapper.text()).toContain("Kostenmodell Pfandschein 2.");
  });

  it("allows approving kyc for a selected customer before origination", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    authStore.token = "token-123";

    vi.spyOn(originationApi, "fetchValuationGuidelines").mockResolvedValue([]);
    vi.spyOn(customerApi, "searchCustomers").mockResolvedValue([
      {
        id: "customer-berlin-2",
        customerNumber: "KD-1002",
        displayName: "Murat Yilmaz",
        phone: "+49 170 222222",
        kycStatus: "IN_PROGRESS",
        kycApproved: false
      }
    ]);
    amlApi.fetchAmlStatus.mockResolvedValueOnce({
      customerId: "customer-berlin-2",
      status: "REVIEW_REQUIRED",
      riskLevel: "HIGH",
      pepFlag: false,
      sanctionsHit: false,
      unusualTransactionFlag: true,
      sourceOfFundsChecked: false,
      suspiciousActivityReported: false,
      goamlReference: null,
      decisionReason: "AML review required before loan origination",
      featureAvailable: true,
      originationAllowed: false
    });
    vi.spyOn(kycApi, "fetchKycStatus").mockResolvedValue({
      customerId: "customer-berlin-2",
      verificationMode: "MANUAL",
      status: "IN_PROGRESS",
      verifiedUntil: "2027-03-18",
      documentType: "PERSONALAUSWEIS",
      documentNumber: "L02X00T48",
      documentValidUntil: "2030-03-18",
      documentFrontImageDataUrl: "data:image/png;base64,front-2",
      documentBackImageDataUrl: "data:image/png;base64,back-2",
      decisionNote: "Dokument geprueft"
    });
    vi.spyOn(pawnTicketApi, "fetchPawnTicketQuote").mockResolvedValue({
      contractNumber: "QUOTE",
      contractBarcode: "QUOTE",
      termsVersion: "AGB-2026-03",
      termsAndConditionsText: "AGB",
      dueDate: "2026-06-18",
      earliestAuctionDate: "2026-07-18",
      monthlyInterestRate: 1,
      monthlyOperatingFee: 4.5,
      totalInterestAmount: 6,
      totalOperatingFeeAmount: 13.5,
      totalRepaymentAmount: 219.5,
      legalText: "Kostenmodell gemaess PfandlV.",
      positions: []
    });
    const updateKycSpy = vi.spyOn(kycApi, "updateKycStatus").mockResolvedValue({
      customerId: "customer-berlin-2",
      status: "APPROVED",
      verifiedUntil: "2027-03-18",
      documentType: "PERSONALAUSWEIS",
      decisionNote: "Freigegeben"
    });

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.selectedCustomerOption = wrapper.vm.customerOptions[0];
    wrapper.vm.handleCustomerSelection({ value: wrapper.vm.customerOptions[0] });
    await flushPromises();

    const approveButton = wrapper.findAll("button").find((button) => button.text().includes("KYC manuell freigeben"));
    await approveButton.trigger("click");
    await flushPromises();
    await wrapper.vm.$nextTick();

    expect(updateKycSpy).toHaveBeenCalled();
    expect(wrapper.vm.selectedCustomer.kycApproved).toBe(true);
    expect(wrapper.vm.selectedCustomer.kycStatus).toBe("APPROVED");
    expect(wrapper.text()).toContain("AML/KYC im Kundenprofil");
  });

  it("shows optional provider verification action when tenant feature is enabled", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    tenantStore.features = [{ tenantId: "tenant-default", featureKey: "kyc-provider-verification", enabled: true }];
    authStore.token = "token-123";

    vi.spyOn(originationApi, "fetchValuationGuidelines").mockResolvedValue([]);
    vi.spyOn(customerApi, "searchCustomers").mockResolvedValue([
      {
        id: "customer-berlin-2",
        customerNumber: "KD-1002",
        displayName: "Murat Yilmaz",
        phone: "+49 170 222222",
        kycStatus: "NOT_STARTED",
        kycApproved: false
      }
    ]);
    vi.spyOn(kycApi, "fetchKycStatus").mockResolvedValue({
      customerId: "customer-berlin-2",
      verificationMode: "MANUAL",
      status: "NOT_STARTED",
      verifiedUntil: null,
      documentType: "PERSONALAUSWEIS",
      documentNumber: "L02X00T48",
      documentValidUntil: "2030-03-18",
      documentFrontImageDataUrl: "data:image/png;base64,front-2",
      documentBackImageDataUrl: "data:image/png;base64,back-2",
      decisionNote: ""
    });
    const updateKycSpy = vi.spyOn(kycApi, "updateKycStatus").mockResolvedValue({
      customerId: "customer-berlin-2",
      verificationMode: "PROVIDER",
      status: "IN_PROGRESS",
      verifiedUntil: null,
      documentType: "PERSONALAUSWEIS",
      decisionNote: "Provider gestartet",
      providerName: "configured-provider",
      providerReference: "provider-customer-berlin-2",
      providerStatus: "PENDING"
    });
    vi.spyOn(pawnTicketApi, "fetchPawnTicketQuote").mockResolvedValue({
      contractNumber: "QUOTE",
      contractBarcode: "QUOTE",
      termsVersion: "AGB-2026-03",
      termsAndConditionsText: "AGB",
      dueDate: "2026-06-18",
      earliestAuctionDate: "2026-07-18",
      monthlyInterestRate: 1,
      monthlyOperatingFee: 4.5,
      totalInterestAmount: 6,
      totalOperatingFeeAmount: 13.5,
      totalRepaymentAmount: 219.5,
      legalText: "Kostenmodell gemaess PfandlV.",
      positions: []
    });

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.selectedCustomerOption = wrapper.vm.customerOptions[0];
    wrapper.vm.handleCustomerSelection({ value: wrapper.vm.customerOptions[0] });
    await flushPromises();

    expect(wrapper.text()).toContain("optionale Feature für externe Ausweisprüfung");

    const providerButton = wrapper.findAll("button").find((button) => button.text().includes("Provider-Prüfung vormerken"));
    await providerButton.trigger("click");
    await flushPromises();

    expect(updateKycSpy).toHaveBeenCalledWith(
      "tenant-default",
      "customer-berlin-2",
      expect.objectContaining({
        verificationMode: "PROVIDER",
        providerStatus: "PENDING"
      }),
      "token-123"
    );
    expect(wrapper.vm.selectedCustomer.verificationMode).toBe("PROVIDER");
    expect(wrapper.vm.selectedCustomer.providerStatus).toBe("PENDING");
  });

  it("requires identity document data for new customers and stores manual kyc before origination", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    tenantStore.features = [{ tenantId: "tenant-default", featureKey: "aml-compliance", enabled: true }];
    authStore.token = "token-123";

    vi.spyOn(originationApi, "fetchValuationGuidelines").mockResolvedValue([
      {
        id: "guideline-gold-585",
        label: "Goldring 585",
        description: "Gelbgold 14 Karat",
        baseLoanValue: 180
      }
    ]);
    vi.spyOn(customerApi, "searchCustomers").mockResolvedValue([]);
    vi.spyOn(customerApi, "createCustomer").mockResolvedValue({
      id: "customer-new",
      customerNumber: "KD-3001",
      firstName: "Lena",
      lastName: "Sommer",
      displayName: "Lena Sommer",
      phone: "+49 170 333333",
      kycStatus: "NOT_STARTED",
      kycApproved: false,
      street: "Beispielweg 3",
      postalCode: "10405",
      city: "Berlin"
    });
    const updateKycSpy = vi.spyOn(kycApi, "updateKycStatus").mockResolvedValue({
      customerId: "customer-new",
      verificationMode: "MANUAL",
      status: "APPROVED",
      verifiedUntil: "2030-03-18",
      documentType: "PERSONALAUSWEIS",
      documentNumber: "C01AB2345",
      documentValidUntil: "2030-03-18",
      documentFrontImageDataUrl: "data:image/png;base64,front",
      documentBackImageDataUrl: "data:image/png;base64,back",
      decisionNote: "Manuell im Beleihungsprozess geprueft"
    });
    const updateAmlSpy = vi.spyOn(amlApi, "updateAmlStatus").mockResolvedValue({
      customerId: "customer-new",
      status: "CLEAR",
      riskLevel: "MEDIUM",
      pepFlag: false,
      sanctionsHit: false,
      unusualTransactionFlag: false,
      sourceOfFundsChecked: true,
      suspiciousActivityReported: false,
      goamlReference: null,
      decisionNote: "AML geprueft",
      featureAvailable: true,
      originationAllowed: true,
      decisionReason: "AML review cleared for origination"
    });
    vi.spyOn(pawnTicketApi, "fetchPawnTicketQuote").mockResolvedValue({
      contractNumber: "QUOTE",
      contractBarcode: "QUOTE",
      termsVersion: "AGB-2026-03",
      termsAndConditionsText: "AGB",
      dueDate: "2026-06-18",
      earliestAuctionDate: "2026-07-18",
      monthlyInterestRate: 1,
      monthlyOperatingFee: 4.5,
      totalInterestAmount: 6,
      totalOperatingFeeAmount: 13.5,
      totalRepaymentAmount: 219.5,
      legalText: "Kostenmodell gemaess PfandlV.",
      positions: []
    });
    const createLoanSpy = vi.spyOn(originationApi, "createLoan").mockResolvedValue({
      id: "loan-1",
      customer: {
        id: "customer-new",
        customerNumber: "KD-3001",
        displayName: "Lena Sommer",
        phone: "+49 170 333333"
      },
      positions: [],
      pawnTickets: []
    });

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.useNewCustomer = true;
    wrapper.vm.newCustomer.customerNumber = "KD-3001";
    wrapper.vm.newCustomer.firstName = "Lena";
    wrapper.vm.newCustomer.lastName = "Sommer";
    wrapper.vm.newCustomer.birthDate = "1991-05-18";
    wrapper.vm.newCustomer.phone = "+49 170 333333";
    wrapper.vm.newCustomer.email = "lena@example.test";
    wrapper.vm.newCustomer.wantsDigitalPawnTicket = true;
    wrapper.vm.newCustomer.street = "Beispielweg 3";
    wrapper.vm.newCustomer.postalCode = "10405";
    wrapper.vm.newCustomer.city = "Berlin";
    wrapper.vm.newCustomerKyc.documentType = "PERSONALAUSWEIS";
    wrapper.vm.newCustomerKyc.documentNumber = "C01AB2345";
    wrapper.vm.newCustomerKyc.documentValidUntil = "2030-03-18";
    wrapper.vm.newCustomerKyc.documentFrontImageDataUrl = "data:image/png;base64,front";
    wrapper.vm.newCustomerKyc.documentBackImageDataUrl = "data:image/png;base64,back";
    wrapper.vm.newCustomerAml.status = "CLEAR";
    wrapper.vm.newCustomerAml.riskLevel = "MEDIUM";
    wrapper.vm.newCustomerAml.sourceOfFundsChecked = true;
    wrapper.vm.positions = [
      {
        ticketGroup: 1,
        label: "Goldring 585",
        description: "Gelbgold 14 Karat",
        guidelineId: "guideline-gold-585",
        pledgedValue: 200
      }
    ];

    await wrapper.vm.submitLoan();
    await flushPromises();

    expect(updateKycSpy).toHaveBeenCalledWith(
      "tenant-default",
      "customer-new",
      expect.objectContaining({
        status: "APPROVED",
        verificationMode: "MANUAL",
        documentNumber: "C01AB2345",
        documentFrontImageDataUrl: "data:image/png;base64,front",
        documentBackImageDataUrl: "data:image/png;base64,back"
      }),
      "token-123"
    );
    expect(updateAmlSpy).toHaveBeenCalledWith(
      "tenant-default",
      "customer-new",
      expect.objectContaining({
        status: "CLEAR",
        riskLevel: "MEDIUM",
        sourceOfFundsChecked: true
      }),
      "token-123"
    );
    expect(createLoanSpy).toHaveBeenCalled();
  });

  it("can prefill new customer identity document data via OCR feature", async () => {
    tenantStore.selectedTenantId = "tenant-default";
    tenantStore.tenants = [{ id: "tenant-default", displayName: "Default Tenant" }];
    tenantStore.features = [{ tenantId: "tenant-default", featureKey: "kyc-document-ocr", enabled: true }];
    authStore.token = "token-123";

    vi.spyOn(originationApi, "fetchValuationGuidelines").mockResolvedValue([]);
    vi.spyOn(customerApi, "searchCustomers").mockResolvedValue([]);
    vi.spyOn(pawnTicketApi, "fetchPawnTicketQuote").mockResolvedValue({
      dueDate: "2026-06-18",
      earliestAuctionDate: "2026-07-18",
      monthlyInterestRate: 1,
      monthlyOperatingFee: 4.5,
      totalInterestAmount: 6,
      totalOperatingFeeAmount: 13.5,
      totalRepaymentAmount: 219.5,
      legalText: "Kostenmodell gemaess PfandlV."
    });
    const prefillSpy = vi.spyOn(kycApi, "prefillKycDocument").mockResolvedValue({
      available: true,
      matched: true,
      documentType: "PERSONALAUSWEIS",
      documentNumber: "XK1234567",
      documentValidUntil: "2031-03-18",
      providerName: "dev-mock-ocr",
      confidence: 0.61
    });

    const wrapper = mountView();
    await flushPromises();

    wrapper.vm.useNewCustomer = true;
    wrapper.vm.newCustomerKyc.documentFrontImageDataUrl = "data:text/plain;base64,RE9DTk86IFhLMTIzNDU2Nw==";
    wrapper.vm.newCustomerKyc.documentBackImageDataUrl = "data:text/plain;base64,VkFMSURfVU5USUw6IDIwMzEtMDMtMTg=";

    await wrapper.vm.prefillNewCustomerDocumentData();
    await flushPromises();

    expect(prefillSpy).toHaveBeenCalledWith(
      "tenant-default",
      "new-customer",
      {
        documentFrontImageDataUrl: "data:text/plain;base64,RE9DTk86IFhLMTIzNDU2Nw==",
        documentBackImageDataUrl: "data:text/plain;base64,VkFMSURfVU5USUw6IDIwMzEtMDMtMTg="
      },
      "token-123"
    );
    expect(wrapper.vm.newCustomerKyc.documentNumber).toBe("XK1234567");
    expect(wrapper.vm.newCustomerKyc.documentValidUntil).toBe("2031-03-18");
  });
});
