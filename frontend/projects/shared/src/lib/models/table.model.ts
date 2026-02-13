export interface RestaurantTable {
  id: number;
  tableNumber: number;
  capacity: number;
  status: TableStatus;
  section: string;
}

export type TableStatus = 'AVAILABLE' | 'OCCUPIED' | 'RESERVED' | 'MAINTENANCE';
