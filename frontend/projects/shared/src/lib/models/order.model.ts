export interface Order {
  id: number;
  orderNumber: string;
  tableId: number;
  tableNumber: number;
  waiterId: number;
  waiterName: string;
  status: OrderStatus;
  orderType: OrderType;
  items: OrderItem[];
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  specialInstructions: string;
  createdAt: string;
  completedAt: string;
}

export interface OrderItem {
  id: number;
  menuItemId: number;
  menuItemName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  specialRequests: string;
  status: ItemStatus;
}

export interface CreateOrderRequest {
  tableId: number;
  items: OrderItemRequest[];
  orderType?: string;
  specialInstructions?: string;
}

export interface OrderItemRequest {
  menuItemId: number;
  quantity: number;
  specialRequests?: string;
}

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'READY' | 'SERVED' | 'COMPLETED' | 'CANCELLED';
export type OrderType = 'DINE_IN' | 'TAKEAWAY' | 'DELIVERY';
export type ItemStatus = 'PENDING' | 'PREPARING' | 'READY' | 'SERVED' | 'CANCELLED';
