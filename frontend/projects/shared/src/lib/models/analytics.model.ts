export interface RecentOrderSummary {
  id: number;
  orderNumber: string;
  status: string;
  totalAmount: number;
  createdAt: string;
}

export interface DashboardStats {
  totalOrders: number;
  totalRevenue: number;
  averageOrderValue: number;
  activeOrders: number;
  tablesOccupied: number;
  totalTables: number;
  ordersByStatus: Record<string, number>;
  ordersByType: Record<string, number>;
  ordersByPaymentMethod?: Record<string, number>;
  topSellingItems: TopItem[];
  recentOrders?: RecentOrderSummary[];
}

export interface TopItem {
  itemId: number;
  itemName: string;
  quantitySold: number;
  revenue: number;
}

export interface SalesReport {
  dailySales: DailySales[];
  revenueByCategory: CategoryRevenue[];
  totalRevenue: number;
  totalOrders: number;
}

export interface DailySales {
  date: string;
  orders: number;
  revenue: number;
}

export interface CategoryRevenue {
  category: string;
  revenue: number;
}

export interface MenuAnalytics {
  totalItems: number;
  availableItems: number;
  topSelling: TopItem[];
  bottomSelling: TopItem[];
  revenueByCategory: CategoryRevenue[];
}

export type ReportType = 'SALES' | 'MENU' | 'CUSTOMERS' | 'STAFF' | 'INVENTORY';
export type ExportFormat = 'PDF' | 'CSV' | 'EXCEL';
