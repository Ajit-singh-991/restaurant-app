import { Component, OnInit, ViewChild } from '@angular/core';
import {
  AnalyticsService, AdvancedAnalyticsService, DashboardStats, SalesReport,
  TopItem, CategoryRevenue, RecentOrderSummary
} from '@shared';
import { ChartConfiguration, ChartData } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

@Component({
  selector: 'app-dashboard-home',
  template: `
    <div class="dashboard-container">
      <div class="dashboard-header">
        <h2>Dashboard</h2>
        <div class="date-range">
          <button mat-stroked-button (click)="setRange(7)" [color]="days === 7 ? 'primary' : ''">7 Days</button>
          <button mat-stroked-button (click)="setRange(14)" [color]="days === 14 ? 'primary' : ''">14 Days</button>
          <button mat-stroked-button (click)="setRange(30)" [color]="days === 30 ? 'primary' : ''">30 Days</button>
          <mat-form-field appearance="outline" class="date-field">
            <mat-label>From</mat-label>
            <input matInput type="date" [(ngModel)]="startDate">
          </mat-form-field>
          <mat-form-field appearance="outline" class="date-field">
            <mat-label>To</mat-label>
            <input matInput type="date" [(ngModel)]="endDate">
          </mat-form-field>
          <button mat-stroked-button (click)="applyCustomRange()">Apply</button>
        </div>
      </div>

      <!-- Loading -->
      <div *ngIf="loading" class="loading">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <ng-container *ngIf="!loading && stats">
        <!-- Stats cards -->
        <div class="stats-grid">
          <mat-card class="stat-card">
            <mat-icon class="stat-icon" color="primary">receipt</mat-icon>
            <div class="stat-value">{{ stats.totalOrders }}</div>
            <div class="stat-label">Orders Today</div>
          </mat-card>
          <mat-card class="stat-card">
            <mat-icon class="stat-icon" color="accent">currency_rupee</mat-icon>
            <div class="stat-value">{{ stats.totalRevenue | currency:'INR':'symbol':'1.0-0' }}</div>
            <div class="stat-label">Revenue Today</div>
          </mat-card>
          <mat-card class="stat-card">
            <mat-icon class="stat-icon" style="color: #ff9800">trending_up</mat-icon>
            <div class="stat-value">{{ stats.averageOrderValue | currency:'INR':'symbol':'1.0-0' }}</div>
            <div class="stat-label">Avg Order Value</div>
          </mat-card>
          <mat-card class="stat-card">
            <mat-icon class="stat-icon" color="warn">pending</mat-icon>
            <div class="stat-value">{{ stats.activeOrders }}</div>
            <div class="stat-label">Active Orders</div>
          </mat-card>
          <mat-card class="stat-card">
            <mat-icon class="stat-icon" style="color: #4caf50">table_restaurant</mat-icon>
            <div class="stat-value">{{ stats.tablesOccupied }}/{{ stats.totalTables }}</div>
            <div class="stat-label">Tables Occupied</div>
          </mat-card>
        </div>

        <!-- Charts row -->
        <div class="charts-row">
          <mat-card class="chart-card wide">
            <h3>Sales Trend</h3>
            <canvas baseChart *ngIf="salesChartData"
                    [data]="salesChartData"
                    [options]="salesChartOptions"
                    type="line">
            </canvas>
            <p *ngIf="!salesChartData" class="no-data">No sales data for this period</p>
          </mat-card>

          <mat-card class="chart-card">
            <h3>Revenue by Category</h3>
            <canvas baseChart *ngIf="categoryChartData"
                    [data]="categoryChartData"
                    [options]="pieChartOptions"
                    type="doughnut">
            </canvas>
            <p *ngIf="!categoryChartData" class="no-data">No category data</p>
          </mat-card>
        </div>

        <!-- Second row -->
        <div class="charts-row">
          <mat-card class="chart-card">
            <h3>Top Selling Items</h3>
            <canvas baseChart *ngIf="topItemsChartData"
                    [data]="topItemsChartData"
                    [options]="barChartOptions"
                    type="bar">
            </canvas>
            <p *ngIf="!topItemsChartData" class="no-data">No items data</p>
          </mat-card>

          <mat-card class="chart-card">
            <h3>Orders by Type</h3>
            <canvas baseChart *ngIf="orderTypeChartData"
                    [data]="orderTypeChartData"
                    [options]="pieChartOptions"
                    type="doughnut">
            </canvas>
            <p *ngIf="!orderTypeChartData" class="no-data">No order data</p>
          </mat-card>
        </div>

        <!-- Third row: Orders by Hour, Payment Methods -->
        <div class="charts-row">
          <mat-card class="chart-card">
            <h3>Orders by Hour</h3>
            <canvas baseChart *ngIf="ordersByHourChartData"
                    [data]="ordersByHourChartData"
                    [options]="barChartOptions"
                    type="bar">
            </canvas>
            <p *ngIf="!ordersByHourChartData" class="no-data">No hourly data</p>
          </mat-card>
          <mat-card class="chart-card">
            <h3>Payment Methods (Today)</h3>
            <canvas baseChart *ngIf="paymentMethodsChartData"
                    [data]="paymentMethodsChartData"
                    [options]="pieChartOptions"
                    type="doughnut">
            </canvas>
            <p *ngIf="!paymentMethodsChartData" class="no-data">No payment data</p>
          </mat-card>
        </div>

        <!-- Recent Orders table -->
        <mat-card class="recent-orders-card" *ngIf="stats.recentOrders?.length">
          <h3>Recent Orders</h3>
          <table mat-table [dataSource]="stats.recentOrders || []" class="recent-table">
            <ng-container matColumnDef="orderNumber">
              <th mat-header-cell *matHeaderCellDef>Order #</th>
              <td mat-cell *matCellDef="let row">{{ row.orderNumber }}</td>
            </ng-container>
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let row">{{ row.status }}</td>
            </ng-container>
            <ng-container matColumnDef="totalAmount">
              <th mat-header-cell *matHeaderCellDef>Amount</th>
              <td mat-cell *matCellDef="let row">{{ row.totalAmount | currency:'INR':'symbol':'1.0-0' }}</td>
            </ng-container>
            <ng-container matColumnDef="createdAt">
              <th mat-header-cell *matHeaderCellDef>Time</th>
              <td mat-cell *matCellDef="let row">{{ row.createdAt | date:'short' }}</td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="recentOrdersColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: recentOrdersColumns;"></tr>
          </table>
        </mat-card>

        <!-- Orders by status -->
        <mat-card class="status-card" *ngIf="stats.ordersByStatus">
          <h3>Orders by Status (Today)</h3>
          <div class="status-chips">
            <div *ngFor="let entry of statusEntries" class="status-chip">
              <span class="status-label">{{ entry[0] }}</span>
              <span class="status-count">{{ entry[1] }}</span>
            </div>
          </div>
        </mat-card>
      </ng-container>
    </div>
  `,
  styles: [`
    .dashboard-container { padding: 16px; max-width: 1200px; margin: 0 auto; }
    .dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .dashboard-header h2 { margin: 0; }
    .date-range { display: flex; gap: 8px; }
    .loading { display: flex; justify-content: center; padding: 48px; }

    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; margin-bottom: 24px; }
    .stat-card { text-align: center; padding: 20px; }
    .stat-icon { font-size: 32px; width: 32px; height: 32px; }
    .stat-value { font-size: 26px; font-weight: 600; margin: 8px 0 4px; }
    .stat-label { color: #666; font-size: 13px; }

    .charts-row { display: grid; grid-template-columns: 1.5fr 1fr; gap: 16px; margin-bottom: 16px; }
    .chart-card { padding: 20px; }
    .chart-card h3 { margin: 0 0 12px; }
    .chart-card.wide { grid-column: span 1; }
    .no-data { color: #999; text-align: center; padding: 24px; }

    .status-card { padding: 20px; margin-bottom: 16px; }
    .status-card h3 { margin: 0 0 12px; }
    .status-chips { display: flex; flex-wrap: wrap; gap: 12px; }
    .status-chip { background: #f5f5f5; border-radius: 20px; padding: 8px 16px; display: flex; gap: 8px; align-items: center; }
    .status-label { color: #666; font-size: 13px; }
    .status-count { font-weight: 600; font-size: 16px; }

    .recent-orders-card { padding: 20px; margin-bottom: 16px; }
    .recent-orders-card h3 { margin: 0 0 12px; }
    .recent-table { width: 100%; }
    .recent-table th, .recent-table td { padding: 8px 16px; }

    @media (max-width: 768px) {
      .charts-row { grid-template-columns: 1fr; }
      .stats-grid { grid-template-columns: repeat(2, 1fr); }
    }
  `]
})
export class DashboardHomeComponent implements OnInit {
  stats: DashboardStats | null = null;
  salesChartData: ChartData<'line'> | null = null;
  categoryChartData: ChartData<'doughnut'> | null = null;
  topItemsChartData: ChartData<'bar'> | null = null;
  orderTypeChartData: ChartData<'doughnut'> | null = null;
  ordersByHourChartData: ChartData<'bar'> | null = null;
  paymentMethodsChartData: ChartData<'doughnut'> | null = null;
  loading = true;
  days = 7;
  startDate: string;
  endDate: string;
  recentOrdersColumns: string[] = ['orderNumber', 'status', 'totalAmount', 'createdAt'];

  salesChartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true } }
  };

  pieChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    plugins: { legend: { position: 'bottom' } }
  };

  barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    indexAxis: 'y',
    plugins: { legend: { display: false } },
    scales: { x: { beginAtZero: true } }
  };

  get statusEntries(): [string, number][] {
    return this.stats ? Object.entries(this.stats.ordersByStatus) : [];
  }

  constructor(
    private analyticsService: AnalyticsService,
    private advancedAnalyticsService: AdvancedAnalyticsService
  ) {
    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - 7);
    this.endDate = end.toISOString().slice(0, 10);
    this.startDate = start.toISOString().slice(0, 10);
  }

  ngOnInit(): void {
    this.loadDashboard();
  }

  setRange(days: number): void {
    this.days = days;
    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - days);
    this.endDate = end.toISOString().slice(0, 10);
    this.startDate = start.toISOString().slice(0, 10);
    this.loadSalesReport();
  }

  applyCustomRange(): void {
    this.days = 0;
    this.loadSalesReport();
  }

  private loadDashboard(): void {
    this.loading = true;
    this.analyticsService.getDashboardStats().subscribe({
      next: stats => {
        this.stats = stats;
        this.buildTopItemsChart(stats.topSellingItems);
        this.buildOrderTypeChart(stats.ordersByType);
        this.buildPaymentMethodsChart(stats.ordersByPaymentMethod);
        this.loading = false;
      },
      error: () => this.loading = false
    });
    this.loadSalesReport();
    this.advancedAnalyticsService.getPeakHours().subscribe({
      next: peak => this.buildOrdersByHourChart(peak.hourlyData),
      error: () => {}
    });
  }

  private loadSalesReport(): void {
    const start = this.startDate || new Date(Date.now() - 7 * 86400000).toISOString().slice(0, 10);
    const end = this.endDate || new Date().toISOString().slice(0, 10);

    this.analyticsService.getSalesReport(start, end).subscribe(report => {
      this.buildSalesChart(report);
      this.buildCategoryChart(report.revenueByCategory);
    });
  }

  private buildSalesChart(report: SalesReport): void {
    if (!report.dailySales.length) { this.salesChartData = null; return; }
    this.salesChartData = {
      labels: report.dailySales.map(d => d.date),
      datasets: [{
        data: report.dailySales.map(d => d.revenue),
        borderColor: '#1976d2',
        backgroundColor: 'rgba(25, 118, 210, 0.1)',
        fill: true,
        tension: 0.3
      }]
    };
  }

  private buildCategoryChart(categories: CategoryRevenue[]): void {
    if (!categories.length) { this.categoryChartData = null; return; }
    const colors = ['#1976d2', '#e91e63', '#4caf50', '#ff9800', '#9c27b0', '#00bcd4', '#795548', '#607d8b'];
    this.categoryChartData = {
      labels: categories.map(c => c.category),
      datasets: [{
        data: categories.map(c => c.revenue),
        backgroundColor: colors.slice(0, categories.length)
      }]
    };
  }

  private buildTopItemsChart(items: TopItem[]): void {
    if (!items.length) { this.topItemsChartData = null; return; }
    this.topItemsChartData = {
      labels: items.map(i => i.itemName),
      datasets: [{
        data: items.map(i => i.quantitySold),
        backgroundColor: '#1976d2'
      }]
    };
  }

  private buildOrderTypeChart(ordersByType: Record<string, number>): void {
    const entries = Object.entries(ordersByType || {});
    if (!entries.length) { this.orderTypeChartData = null; return; }
    this.orderTypeChartData = {
      labels: entries.map(e => e[0]),
      datasets: [{
        data: entries.map(e => e[1]),
        backgroundColor: ['#1976d2', '#e91e63', '#4caf50']
      }]
    };
  }

  private buildOrdersByHourChart(hourlyData: { hour: number; orderCount: number; revenue: number }[]): void {
    if (!hourlyData?.length) { this.ordersByHourChartData = null; return; }
    const sorted = [...hourlyData].sort((a, b) => a.hour - b.hour);
    this.ordersByHourChartData = {
      labels: sorted.map(h => `${h.hour}:00`),
      datasets: [{
        data: sorted.map(h => h.orderCount),
        backgroundColor: '#673ab7'
      }]
    };
  }

  private buildPaymentMethodsChart(ordersByPaymentMethod?: Record<string, number>): void {
    const entries = Object.entries(ordersByPaymentMethod || {});
    if (!entries.length) { this.paymentMethodsChartData = null; return; }
    const colors = ['#4caf50', '#2196f3', '#ff9800', '#9c27b0'];
    this.paymentMethodsChartData = {
      labels: entries.map(e => e[0]),
      datasets: [{
        data: entries.map(e => e[1]),
        backgroundColor: colors.slice(0, entries.length)
      }]
    };
  }
}
