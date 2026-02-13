import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { OrderService, WebSocketService, Order } from '@shared';

@Component({
  selector: 'app-waiter-orders',
  template: `
    <div class="orders-container">
      <div class="orders-header">
        <h2>Active Orders</h2>
        <div class="connection-status" [class.connected]="connected">
          <span class="dot"></span>
          {{ connected ? 'Live' : 'Reconnecting...' }}
        </div>
      </div>
      <div class="orders-grid">
        <mat-card *ngFor="let order of orders" class="order-card"
                  [class.ready]="order.status === 'READY'">
          <mat-card-header>
            <mat-card-title>{{ order.orderNumber }}</mat-card-title>
            <mat-card-subtitle>Table {{ order.tableNumber }}</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <mat-chip [color]="order.status === 'READY' ? 'accent' : 'primary'" selected>
              {{ order.status }}
            </mat-chip>
            <p>{{ order.totalAmount | currency:'INR' }}</p>
            <p class="order-time">{{ order.createdAt | date:'shortTime' }}</p>
          </mat-card-content>
          <mat-card-actions>
            <button mat-button color="primary" *ngIf="order.status === 'READY'"
                    (click)="markServed(order)">Mark Served</button>
            <button mat-button color="accent" *ngIf="order.status === 'SERVED' || order.status === 'COMPLETED'"
                    (click)="viewBill(order)">
              <mat-icon>receipt</mat-icon> Bill
            </button>
          </mat-card-actions>
        </mat-card>
      </div>
      <div *ngIf="orders.length === 0" class="empty-state">
        <mat-icon>restaurant</mat-icon>
        <p>No active orders</p>
      </div>
    </div>
  `,
  styles: [`
    .orders-container { padding: 16px; }
    .orders-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .orders-header h2 { margin: 0; }
    .connection-status { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #999; }
    .connection-status.connected { color: #4caf50; }
    .dot { width: 8px; height: 8px; border-radius: 50%; background: #999; }
    .connected .dot { background: #4caf50; }
    .orders-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 16px; }
    .order-card.ready { border-left: 4px solid #4caf50; }
    .order-time { font-size: 12px; color: #888; }
    .empty-state { text-align: center; padding: 48px; color: #666; }
    .empty-state mat-icon { font-size: 48px; width: 48px; height: 48px; }
  `]
})
export class WaiterOrdersComponent implements OnInit, OnDestroy {
  orders: Order[] = [];
  connected = false;
  private subs = new Subscription();

  constructor(
    private orderService: OrderService,
    private wsService: WebSocketService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadOrders();

    // WebSocket real-time updates
    this.wsService.connect();
    this.subs.add(
      this.wsService.connected$.subscribe(state => this.connected = state === 1)
    );
    this.subs.add(
      this.wsService.subscribeToOrders().subscribe(() => this.loadOrders())
    );
    this.subs.add(
      this.wsService.subscribeToWaiter().subscribe(notification => {
        if (notification?.type === 'ORDER_READY') {
          this.snackBar.open(notification.message, 'View', { duration: 5000 })
            .onAction().subscribe(() => this.loadOrders());
        }
        this.loadOrders();
      })
    );

    // Fallback polling
    this.subs.add(
      interval(30000).pipe(
        switchMap(() => this.orderService.getActiveOrders())
      ).subscribe(orders => this.orders = orders)
    );
  }

  loadOrders(): void {
    this.orderService.getActiveOrders().subscribe(orders => this.orders = orders);
  }

  markServed(order: Order): void {
    this.orderService.updateOrderStatus(order.id, 'SERVED').subscribe(() => this.loadOrders());
  }

  viewBill(order: Order): void {
    this.router.navigate(['/billing', order.id]);
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.wsService.disconnect();
  }
}
