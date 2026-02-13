export interface Payment {
  id: number;
  orderId: number;
  orderNumber: string;
  amount: number;
  paymentMethod: PaymentMethod;
  transactionId: string;
  status: PaymentStatus;
  createdAt: string;
  refundedAt: string;
  refundReason: string;
}

export interface PaymentRequest {
  orderId: number;
  paymentMethod: PaymentMethod;
  transactionId?: string;
  recipientEmail?: string;
}

export interface RefundRequest {
  reason: string;
}

export interface PaymentStats {
  dailySales: number;
  dailyTransactions: number;
  weeklySales: number;
  weeklyTransactions: number;
  averageOrderValue: number;
}

export type PaymentMethod = 'CASH' | 'CARD' | 'UPI' | 'WALLET';
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';
