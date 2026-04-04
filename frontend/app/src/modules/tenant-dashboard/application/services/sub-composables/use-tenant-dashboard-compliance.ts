import { ref, type Ref } from "vue";
import { 
  updateTenantHomeKycStatus,
  fetchTenantHomeKycStatus,
  assessTenantHomeAmlOrigination
} from "../../../infrastructure/adapters/http-tenant-dashboard.adapter";
import { 
  mergeKycStatus,
  mergeAmlStatus,
  hasRequiredManualKycDocuments as hasRequiredManualKycDocumentsState
} from "../../../domain/mappers";
import type { TenantHomeCustomerModel } from "../../../domain/model/tenant-dashboard";

export function useTenantDashboardCompliance({
  tenantId,
  t,
  amlFeatureEnabled,
  customers
}: {
  tenantId: Ref<string>;
  t: (key: string, params?: Record<string, unknown>) => string;
  amlFeatureEnabled: Ref<boolean>;
  customers: Ref<TenantHomeCustomerModel[]>;
}) {
  const isUpdatingKyc = ref(false);
  const isUpdatingAml = ref(false);

  async function updateSelectedCustomerKyc(
    customerId: string, 
    verificationMode: "MANUAL" | "PROVIDER",
    currentCustomer: TenantHomeCustomerModel | null
  ) {
    if (!tenantId.value || !customerId) return;

    try {
      isUpdatingKyc.value = true;
      
      if (verificationMode === "MANUAL" && currentCustomer) {
        const hasDocs = hasRequiredManualKycDocumentsState({
          documentFrontImageDataUrl: currentCustomer.documentFrontImageDataUrl,
          documentBackImageDataUrl: currentCustomer.documentBackImageDataUrl
        });
        if (!hasDocs) {
          throw new Error(t("tenantHome.messages.documentImagesRequired"));
        }
      }

      const kycStatus = await updateTenantHomeKycStatus(
        tenantId.value,
        customerId,
        verificationMode === "PROVIDER"
          ? {
              status: "IN_PROGRESS",
              verificationMode: "PROVIDER",
              verifiedUntil: null,
              documentType: "PERSONALAUSWEIS",
              decisionNote: "Provider-Prüfung im Tenant-Dashboard vorgemerkt",
              providerName: "configured-provider",
              providerReference: `provider-${customerId}`,
              providerStatus: "PENDING"
            }
          : {
              status: "APPROVED",
              verificationMode: "MANUAL",
              verifiedUntil: currentCustomer?.documentValidUntil ?? null,
              documentType: currentCustomer?.documentType ?? "PERSONALAUSWEIS",
              documentNumber: currentCustomer?.documentNumber ?? "",
              documentValidUntil: currentCustomer?.documentValidUntil ?? null,
              documentFrontImageDataUrl: currentCustomer?.documentFrontImageDataUrl ?? "",
              documentBackImageDataUrl: currentCustomer?.documentBackImageDataUrl ?? "",
              decisionNote: "Manuell im Tenant-Dashboard freigegeben",
              providerName: null,
              providerReference: null,
              providerStatus: null
            }
      );

      customers.value = customers.value.map((c) =>
        c.id === customerId ? mergeKycStatus(c, kycStatus) : c
      );
    } finally {
      isUpdatingKyc.value = false;
    }
  }

  async function loadSelectedCustomerKyc(customerId: string, totalLoanValue: number) {
    if (!tenantId.value || !customerId) return;

    try {
      const [kycStatus, amlStatus] = await Promise.all([
        fetchTenantHomeKycStatus(tenantId.value, customerId),
        amlFeatureEnabled.value
          ? assessTenantHomeAmlOrigination(
              tenantId.value,
              customerId,
              { loanAmount: totalLoanValue }
            )
          : Promise.resolve(null)
      ]);

      customers.value = customers.value.map((c) =>
        c.id === customerId
          ? (amlStatus
              ? mergeAmlStatus(mergeKycStatus(c, kycStatus), amlStatus)
              : mergeKycStatus(c, kycStatus))
          : c
      );
    } catch (error) {
      console.error("[Compliance] Sync failed", error);
    }
  }

  return {
    customers,
    isUpdatingKyc,
    isUpdatingAml,
    updateSelectedCustomerKyc,
    loadSelectedCustomerKyc,
    startProviderVerification: async (customerId: string) => {
      await updateSelectedCustomerKyc(customerId, "PROVIDER", null);
    }
  };
}
