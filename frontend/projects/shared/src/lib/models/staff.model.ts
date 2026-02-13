export interface TimeEntry {
  id: number;
  staffId: number;
  staffName: string;
  role: string;
  clockIn: string;
  clockOut: string | null;
  hoursWorked: number | null;
  tips: number;
  notes: string | null;
}

export interface StaffPerformance {
  staffId: number;
  staffName: string;
  role: string;
  totalHoursWorked: number;
  totalTips: number;
  ordersHandled: number;
  shiftsCount: number;
}

export interface StaffSummary {
  activeSessions: number;
  totalHoursToday: number;
  totalTipsToday: number;
  staffPerformance: StaffPerformance[];
}
