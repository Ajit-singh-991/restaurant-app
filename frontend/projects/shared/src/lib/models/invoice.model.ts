export interface Invoice {
  id: number;
  orderId: number;
  invoiceNumber: string;
  billToName: string;
  billToContact: string;
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  generatedAt: string;
}
