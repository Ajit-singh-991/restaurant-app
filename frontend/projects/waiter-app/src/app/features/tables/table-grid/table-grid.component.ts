import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TableService, OrderService, RestaurantTable } from '@shared';

@Component({
  selector: 'app-table-grid',
  template: `
    <div class="tables-container">
      <h2>Table Overview</h2>
      <div class="table-grid">
        <mat-card *ngFor="let table of tables"
                  class="table-card"
                  [ngClass]="'status-' + table.status.toLowerCase()">
          <mat-card-header>
            <mat-card-title>Table {{ table.tableNumber }}</mat-card-title>
            <mat-card-subtitle>{{ table.section }}</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <p><mat-icon inline>people</mat-icon> {{ table.capacity }} seats</p>
            <mat-chip [color]="getStatusColor(table.status)" selected>
              {{ table.status }}
            </mat-chip>
          </mat-card-content>
          <mat-card-actions>
            <button mat-button *ngIf="table.status === 'AVAILABLE'" (click)="takeOrder(table)">
              Take Order
            </button>
            <button mat-button *ngIf="table.status === 'OCCUPIED'" (click)="viewOrder(table)">
              View Order / Bill
            </button>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .tables-container { padding: 16px; }
    .table-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; }
    .table-card { text-align: center; cursor: pointer; transition: transform 0.2s; }
    .table-card:hover { transform: scale(1.02); }
    .status-available { border-left: 4px solid #4caf50; }
    .status-occupied { border-left: 4px solid #f44336; }
    .status-reserved { border-left: 4px solid #ff9800; }
    .status-maintenance { border-left: 4px solid #9e9e9e; }
  `]
})
export class TableGridComponent implements OnInit {
  tables: RestaurantTable[] = [];

  constructor(
    private tableService: TableService,
    private orderService: OrderService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.tableService.getAllTables().subscribe(tables => this.tables = tables);
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      AVAILABLE: 'primary', OCCUPIED: 'warn', RESERVED: 'accent', MAINTENANCE: ''
    };
    return colors[status] || '';
  }

  takeOrder(table: RestaurantTable): void {
    this.router.navigate(['/orders']);
  }

  viewOrder(table: RestaurantTable): void {
    this.orderService.getActiveTableOrders(table.id).subscribe({
      next: (orders) => {
        if (orders.length > 0) {
          this.router.navigate(['/billing', orders[0].id]);
        } else {
          this.snackBar.open('No active order for this table', 'OK', { duration: 3000 });
        }
      },
      error: () => this.snackBar.open('Could not load orders', 'OK')
    });
  }
}
