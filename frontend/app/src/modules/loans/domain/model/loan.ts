export interface LoanModel {
  id: string;
  customer: {
    id?: string;
    customerNumber: string;
    displayName: string;
  };
  pledgeRecord: {
    recordedAt: string;
    checkedDocumentType?: string | null;
    powerOfAttorneyRequired: boolean;
    retentionUntil: string;
  };
  pawnTickets: Array<{
    ticketNumber: string;
    totalLoanValue: number;
  }>;
}
