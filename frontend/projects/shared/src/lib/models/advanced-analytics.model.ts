export interface SalesForecast {
  forecast: ForecastPoint[];
  projectedWeeklyRevenue: number;
  projectedMonthlyRevenue: number;
  growthTrendPercent: number;
}

export interface ForecastPoint {
  date: string;
  predictedRevenue: number;
  predictedOrders: number;
}

export interface CustomerSegmentation {
  segments: CustomerSegment[];
  totalCustomers: number;
}

export interface CustomerSegment {
  segment: string;
  customerCount: number;
  avgOrderValue: number;
  avgOrderFrequency: number;
  totalRevenue: number;
}

export interface MenuOptimization {
  suggestions: MenuSuggestion[];
}

export interface MenuSuggestion {
  itemId: number;
  itemName: string;
  suggestion: string;
  reason: string;
  priority: string;
}

export interface PeakHoursAnalysis {
  hourlyData: HourlyData[];
  peakHour: number;
  slowestHour: number;
}

export interface HourlyData {
  hour: number;
  orderCount: number;
  revenue: number;
}
