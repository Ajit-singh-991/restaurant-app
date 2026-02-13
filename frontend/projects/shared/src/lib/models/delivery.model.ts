export interface DeliveryTracking {
  id: number;
  orderId: number;
  orderNumber: string;
  driverId?: number;
  driverName?: string;
  status: DeliveryStatus;
  deliveryAddress: string;
  city: string;
  postalCode: string;
  deliveryInstructions?: string;
  contactPhone: string;
  deliveryFee: number;
  estimatedMinutes?: number;
  assignedAt?: string;
  pickedUpAt?: string;
  deliveredAt?: string;
  createdAt: string;
}

export type DeliveryStatus = 'PENDING' | 'ASSIGNED' | 'PICKED_UP' | 'IN_TRANSIT' | 'DELIVERED' | 'FAILED' | 'CANCELLED';

export interface CreateDeliveryRequest {
  orderId: number;
  deliveryAddress: string;
  city: string;
  postalCode: string;
  deliveryInstructions?: string;
  contactPhone: string;
}

export interface AssignDriverRequest {
  driverId: number;
  estimatedMinutes: number;
}

export interface DeliveryStats {
  totalDeliveries: number;
  activeDeliveries: number;
  completedToday: number;
  avgDeliveryTimeMinutes: number;
  driverPerformance: DriverPerformance[];
}

export interface DriverPerformance {
  driverId: number;
  driverName: string;
  deliveriesCompleted: number;
  avgDeliveryTimeMinutes: number;
  activeDeliveries: number;
}
