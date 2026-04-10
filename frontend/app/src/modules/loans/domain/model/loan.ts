export interface LoanModel {
  id: string;
  customer: {
    id?: string;
    customerNumber: string;
    displayName: string;
  };
  pledgeRecord: {
    recordedAt: string;
    languageCode: string;
    checkedDocumentType?: string | null;
    powerOfAttorneyRequired: boolean;
    bearerName: string | null;
    retentionUntil: string;
  };
  positions: Array<{
    positionNumber: number;
    description: string;
    weightGram: number | null;
    purity: string | null;
    estimatedValue: number;
    loanAmount: number;
  }>;
  pawnTickets: Array<{
    ticketNumber: string;
    totalLoanValue: number;
    dueDate: string;
  }>;
}
