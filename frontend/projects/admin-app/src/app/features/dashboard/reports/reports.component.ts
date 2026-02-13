import { Component } from '@angular/core';
import { AnalyticsService } from '@shared';
import type { ReportType, ExportFormat } from '@shared';

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.scss']
})
export class ReportsComponent {
  reportType: ReportType = 'SALES';
  format: ExportFormat = 'PDF';
  startDate: string;
  endDate: string;
  loading = false;
  error: string | null = null;

  reportTypes: { value: ReportType; label: string }[] = [
    { value: 'SALES', label: 'Sales' },
    { value: 'MENU', label: 'Menu' },
    { value: 'CUSTOMERS', label: 'Customers' },
    { value: 'STAFF', label: 'Staff' },
    { value: 'INVENTORY', label: 'Inventory' }
  ];

  formats: { value: ExportFormat; label: string }[] = [
    { value: 'PDF', label: 'PDF' },
    { value: 'CSV', label: 'CSV' },
    { value: 'EXCEL', label: 'Excel' }
  ];

  constructor(private analytics: AnalyticsService) {
    const end = new Date();
    const start = new Date();
    start.setDate(start.getDate() - 30);
    this.endDate = this.toIsoDate(end);
    this.startDate = this.toIsoDate(start);
  }

  private toIsoDate(d: Date): string {
    return d.toISOString().slice(0, 10);
  }

  setRange(days: number): void {
    const end = new Date();
    const start = new Date();
    start.setDate(start.getDate() - days);
    this.startDate = this.toIsoDate(start);
    this.endDate = this.toIsoDate(end);
  }

  generate(): void {
    this.error = null;
    if (!this.startDate || !this.endDate) {
      this.error = 'Please select start and end date.';
      return;
    }
    if (new Date(this.startDate) > new Date(this.endDate)) {
      this.error = 'Start date must be before end date.';
      return;
    }
    this.loading = true;
    this.analytics.exportReport(this.reportType, this.format, this.startDate, this.endDate).subscribe({
      next: (blob) => {
        this.loading = false;
        const ext = this.format === 'CSV' ? 'csv' : this.format === 'PDF' ? 'pdf' : 'xlsx';
        const filename = `${this.reportType.toLowerCase()}-report.${ext}`;
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Export failed.';
      }
    });
  }
}
