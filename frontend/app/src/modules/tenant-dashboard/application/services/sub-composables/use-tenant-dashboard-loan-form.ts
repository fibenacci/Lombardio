import { computed, reactive, ref, type Ref } from "vue";
import { 
  fetchTenantHomeGuidelines, 
  fetchTenantHomeQuote 
} from "../../../infrastructure/adapters/http-tenant-dashboard.adapter";
import { createEmptyPosition } from "../../../domain/mappers";
import type { TenantHomePositionModel } from "../../../domain/model/tenant-dashboard";
import type { TenantHomeGuidelineDto } from "../../../infrastructure/dto/tenant-dashboard.dto";
import { readFileAsDataUrl, firstSelectedFile } from "../tenant-dashboard-file.utils";

export function useTenantDashboardLoanForm({
  tenantId,
  t
}: {
  tenantId: Ref<string>;
  t: (key: string, params?: Record<string, unknown>) => string;
}) {
  const guidelines = ref<TenantHomeGuidelineDto[]>([]);
  const loanQuotes = ref<Array<Record<string, unknown>>>([]);
  
  const terms = reactive({
    termMonths: 3,
    manualMonthlyOperatingFee: ""
  });

  const positions = ref<TenantHomePositionModel[]>([createEmptyPosition()]);

  const pledgePresentation = reactive({
    thirdPartyPledgorPresentation: false,
    bearerName: "",
    bearerStreet: "",
    bearerPostalCode: "",
    bearerCity: "",
    powerOfAttorneyDocumentDataUrl: ""
  });

  const guidelineOptions = computed(() =>
    guidelines.value.map((g) => ({
      value: g.id || "",
      label: g.label || ""
    }))
  );

  const totalLoanValue = computed(() =>
    positions.value.reduce((total, p) => total + Number(p.pledgedValue || 0), 0).toFixed(2)
  );

  async function loadGuidelines() {
    if (!tenantId.value) return;
    try {
      guidelines.value = await fetchTenantHomeGuidelines(tenantId.value);
    } catch (error) {
      console.error("[LoanForm] Failed to load guidelines", error);
    }
  }

  async function refreshQuote() {
    if (!tenantId.value) {
      loanQuotes.value = [];
      return;
    }

    const groupedPositions = positions.value
      .filter((p) => Number(p.pledgedValue) > 0 && Number(p.ticketGroup) >= 1)
      .reduce((groups, p) => {
        const group = Number(p.ticketGroup);
        if (!groups.has(group)) groups.set(group, []);
        groups.get(group)?.push(p);
        return groups;
      }, new Map<number, typeof positions.value>());

    if (!groupedPositions.size) {
      loanQuotes.value = [];
      return;
    }

    try {
      loanQuotes.value = await Promise.all(
        [...groupedPositions.entries()]
          .sort(([left], [right]) => left - right)
          .map(async ([ticketGroup, ticketPositions]) => {
            const loanAmount = ticketPositions.reduce(
              (sum, p) => sum + Number(p.pledgedValue || 0),
              0
            );

            const quote = await fetchTenantHomeQuote({
              loanAmount,
              termMonths: Number(terms.termMonths),
              manualMonthlyOperatingFee: terms.manualMonthlyOperatingFee
                ? Number(terms.manualMonthlyOperatingFee)
                : null
            });

            return {
              ...quote,
              ticketGroup,
              positionCount: ticketPositions.length,
              totalLoanValue: loanAmount
            };
          })
      );
    } catch (error) {
      console.error("[LoanForm] Refresh quote failed", error);
    }
  }

  function addPosition() {
    const nextGroup = positions.value.reduce((max, p) => Math.max(max, Number(p.ticketGroup) || 0), 0) + 1;
    positions.value = [...positions.value, { ...createEmptyPosition(), ticketGroup: nextGroup }];
  }

  function removePosition(index: number) {
    if (positions.value.length === 1) return;
    positions.value = positions.value.filter((_, i) => i !== index);
  }

  function applyGuideline(index: number, guidelineId: string) {
    const g = guidelines.value.find((item) => item.id === guidelineId);
    if (!g) return;

    positions.value[index].guidelineId = guidelineId;
    if (!positions.value[index].label) positions.value[index].label = g.label || "";
    if (!positions.value[index].description) positions.value[index].description = g.description || "";
    if (!positions.value[index].pledgedValue) positions.value[index].pledgedValue = String(g.baseLoanValue || "");
  }

  async function updatePowerOfAttorney(event: unknown) {
    const file = firstSelectedFile(event);
    if (!file) {
      pledgePresentation.powerOfAttorneyDocumentDataUrl = "";
      return;
    }
    try {
      pledgePresentation.powerOfAttorneyDocumentDataUrl = await readFileAsDataUrl(file, t);
    } catch (error) {
      console.error("[LoanForm] Power of attorney upload failed", error);
    }
  }

  return {
    guidelines,
    guidelineOptions,
    loanQuotes,
    terms,
    positions,
    pledgePresentation,
    totalLoanValue,
    loadGuidelines,
    refreshQuote,
    addPosition,
    removePosition,
    applyGuideline,
    updatePowerOfAttorneyDocument: updatePowerOfAttorney,
    clearPowerOfAttorneyDocument: () => {
      pledgePresentation.powerOfAttorneyDocumentDataUrl = "";
    }
  };
}
