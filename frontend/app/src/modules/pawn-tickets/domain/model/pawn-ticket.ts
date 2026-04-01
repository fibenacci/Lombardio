export interface PawnTicketModel {
  contractNumber: string;
  ticketNumber: string;
  contractBarcode: string;
  termsVersion: string;
  customerNumber: string;
  customerDisplayName: string;
  createdAt: string;
  dueDate: string;
  earliestAuctionDate: string;
  totalLoanValue: number;
  totalRepaymentAmount: number;
  positionCount: number;
  status?: string;
}
