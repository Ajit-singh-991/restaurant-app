import { Component, OnInit } from '@angular/core';
import { OrderService, Order } from '@shared';

@Component({
  selector: 'app-order-list',
  template: `
    <h2>Order Management</h2>
    <table mat-table [dataSource]="orders" class="full-width">
      <ng-container matColumnDef="orderNumber">
        <th mat-header-cell *matHeaderCellDef>Order #</th>
        <td mat-cell *matCellDef="let order">{{ order.orderNumber }}</td>
      </ng-container>
      <ng-container matColumnDef="table">
        <th mat-header-cell *matHeaderCellDef>Table</th>
        <td mat-cell *matCellDef="let order">{{ order.tableNumber || 'N/A' }}</td>
      </ng-container>
      <ng-container matColumnDef="status">
        <th mat-header-cell *matHeaderCellDef>Status</th>
        <td mat-cell *matCellDef="let order">
          <mat-chip>{{ order.status }}</mat-chip>
        </td>
      </ng-container>
      <ng-container matColumnDef="total">
        <th mat-header-cell *matHeaderCellDef>Total</th>
        <td mat-cell *matCellDef="let order">{{ order.totalAmount | currency:'INR' }}</td>
      </ng-container>
      <ng-container matColumnDef="date">
        <th mat-header-cell *matHeaderCellDef>Date</th>
        <td mat-cell *matCellDef="let order">{{ order.createdAt | date:'short' }}</td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
      <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
    </table>
  `,
  styles: [`.full-width { width: 100%; }`]
})
export class OrderListComponent implements OnInit {
  orders: Order[] = [];
  displayedColumns = ['orderNumber', 'table', 'status', 'total', 'date'];

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.orderService.getActiveOrders().subscribe(orders => this.orders = orders);
  }
}
