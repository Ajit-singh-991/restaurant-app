import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OrderService, Order, AuthService } from '@shared';

@Component({
  selector: 'app-order-list',
  template: `
    <div class="orders-container">
      <h2>My Orders</h2>
      <div *ngIf="!authService.isAuthenticated()" class="empty-state">
        <mat-icon>login</mat-icon>
        <p>Please log in to see your orders</p>
        <button mat-raised-button color="primary" routerLink="/login">Login</button>
      </div>
      <div *ngIf="authService.isAuthenticated() && loading" class="loading">
        <mat-spinner diameter="40"></mat-spinner>
      </div>
      <div *ngIf="authService.isAuthenticated() && !loading && orders.length === 0" class="empty-state">
        <mat-icon>receipt_long</mat-icon>
        <p>No orders yet</p>
      </div>
      <ng-container *ngIf="authService.isAuthenticated() && !loading">
        <mat-card *ngFor="let order of orders" class="order-card">
        <mat-card-header>
          <mat-card-title>Order #{{ order.orderNumber }}</mat-card-title>
          <mat-card-subtitle>{{ order.createdAt | date:'medium' }}</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <p>Items: {{ order.items?.length }}</p>
          <p>Total: {{ order.totalAmount | currency:'INR' }}</p>
          <mat-chip [color]="getStatusColor(order.status)" selected>
            {{ order.status }}
          </mat-chip>
        </mat-card-content>
      </mat-card>
      </ng-container>
    </div>
  `,
  styles: [`
    .orders-container { max-width: 800px; margin: 24px auto; padding: 0 16px; }
    .order-card { margin-bottom: 16px; }
    .loading, .empty-state { text-align: center; padding: 48px; }
    .empty-state mat-icon { font-size: 48px; width: 48px; height: 48px; color: #ccc; }
  `]
})
export class OrderListComponent implements OnInit {
  orders: Order[] = [];
  loading = true;

  constructor(
    public authService: AuthService,
    private orderService: OrderService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.loading = false;
      return;
    }
    const customerId = this.authService.getUserId();
    if (customerId == null) {
      this.loading = false;
      return;
    }
    this.orderService.getOrdersByCustomer(customerId).subscribe({
      next: orders => { this.orders = orders; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      PENDING: 'warn', CONFIRMED: 'primary', PREPARING: 'accent',
      READY: 'primary', COMPLETED: 'primary', CANCELLED: 'warn'
    };
    return colors[status] || 'primary';
  }
}
