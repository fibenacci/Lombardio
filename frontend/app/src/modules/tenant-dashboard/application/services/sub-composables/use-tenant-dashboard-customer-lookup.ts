import { computed, reactive, ref, type Ref } from "vue";
import { 
  searchTenantHomeCustomers, 
  prefillTenantHomeKycDocument 
} from "../../../infrastructure/adapters/http-tenant-dashboard.adapter";
import { 
  formatCustomerOption, 
  matchesCustomerQuery, 
  toCustomerModel,
  createEmptyNewCustomerKyc,
  createEmptyNewCustomerAml
} from "../../../domain/mappers";
import type { TenantHomeCustomerModel } from "../../../domain/model/tenant-dashboard";
import { readFileAsDataUrl, firstSelectedFile } from "../tenant-dashboard-file.utils";

export function useTenantDashboardCustomerLookup({
  tenantId,
  t,
  ocrAvailable,
  onEnrichCompliance,
  customers = ref<TenantHomeCustomerModel[]>([])
}: {
  tenantId: Ref<string>;
  t: (key: string, params?: Record<string, unknown>) => string;
  ocrAvailable: Ref<boolean>;
  onEnrichCompliance: (customer: TenantHomeCustomerModel) => Promise<TenantHomeCustomerModel>;
  customers?: Ref<TenantHomeCustomerModel[]>;
}) {
  const customerQuery = ref("");
  const customerSuggestions = ref<Array<{ value: string; label: string }>>([]);
  const selectedCustomerId = ref("");
  const selectedCustomerOption = ref<{ value?: string } | null>(null);
  const useNewCustomer = ref(false);

  const newCustomer = reactive({
    customerNumber: "",
    firstName: "",
    lastName: "",
    birthDate: "",
    phone: "",
    email: "",
    wantsDigitalPawnTicket: false,
    street: "",
    postalCode: "",
    city: ""
  });

  const newCustomerKyc = reactive(createEmptyNewCustomerKyc());
  const newCustomerAml = reactive(createEmptyNewCustomerAml());

  const selectedCustomer = computed(() =>
    customers.value.find((c) => c.id === selectedCustomerId.value) ?? null
  );

  const customerOptions = computed(() =>
    customers.value.map((c) => formatCustomerOption(c, t))
  );

  async function searchSuggestions(event: { query?: string } = {}) {
    if (!tenantId.value) return;

    const query = String(event.query ?? customerQuery.value ?? "");
    customerQuery.value = query;

    if (query.trim().length < 2) {
      customerSuggestions.value = customers.value
        .filter((c) => matchesCustomerQuery(c, query))
        .map((c) => formatCustomerOption(c, t));
      return;
    }

    try {
      const response = await searchTenantHomeCustomers(tenantId.value, query);
      customers.value = await Promise.all(
        response.map((c) => onEnrichCompliance(toCustomerModel(c)))
      );
      customerSuggestions.value = customerOptions.value;
    } catch (error) {
      console.error("[CustomerLookup] Search failed", error);
    }
  }

  function handleSelection(event: { value?: { value?: string } } | undefined) {
    const option = event?.value ?? selectedCustomerOption.value;
    selectedCustomerOption.value = option && typeof option === "object" ? option : null;
    selectedCustomerId.value = option?.value ?? "";
  }

  async function updateNewCustomerDocument(side: "documentFrontImageDataUrl" | "documentBackImageDataUrl", event: unknown) {
    const file = firstSelectedFile(event);
    if (!file) {
      newCustomerKyc[side] = "";
      return;
    }

    try {
      newCustomerKyc[side] = await readFileAsDataUrl(file, t);
      if (ocrAvailable.value && newCustomerKyc.documentFrontImageDataUrl) {
        await prefillOcr();
      }
    } catch (error) {
      console.error("[CustomerLookup] File upload failed", error);
    }
  }

  async function prefillOcr() {
    if (!tenantId.value || !newCustomerKyc.documentFrontImageDataUrl) return;

    try {
      const result = await prefillTenantHomeKycDocument(
        tenantId.value,
        "new-customer",
        {
          documentFrontImageDataUrl: newCustomerKyc.documentFrontImageDataUrl,
          documentBackImageDataUrl: newCustomerKyc.documentBackImageDataUrl
        }
      );

      if (result.available && result.matched) {
        newCustomerKyc.documentType = result.documentType ?? newCustomerKyc.documentType;
        newCustomerKyc.documentNumber = result.documentNumber ?? newCustomerKyc.documentNumber;
        newCustomerKyc.documentValidUntil = result.documentValidUntil ?? newCustomerKyc.documentValidUntil;
        newCustomerKyc.portraitImageDataUrl = result.portraitImageDataUrl ?? newCustomerKyc.portraitImageDataUrl;

        if (result.firstName) newCustomer.firstName = result.firstName;
        if (result.lastName) newCustomer.lastName = result.lastName;
        if (result.birthDate) newCustomer.birthDate = result.birthDate;
      }
    } catch (error) {
      console.error("[CustomerLookup] OCR failed", error);
    }
  }

  const canPrefillNewCustomerDocument = computed(() => {
    return !!newCustomerKyc.documentFrontImageDataUrl;
  });

  return {
    customerQuery,
    customers,
    customerSuggestions,
    customerOptions,
    selectedCustomerId,
    selectedCustomerOption,
    selectedCustomer,
    useNewCustomer,
    newCustomer,
    newCustomerKyc,
    newCustomerAml,
    canPrefillNewCustomerDocument,
    searchCustomerSuggestions: searchSuggestions,
    handleCustomerSelection: handleSelection,
    prefillNewCustomerDocumentData: prefillOcr,
    updateNewCustomerDocument,
    clearNewCustomerDocument: (side: "documentFrontImageDataUrl" | "documentBackImageDataUrl") => {
      newCustomerKyc[side] = "";
    }
  };
}
