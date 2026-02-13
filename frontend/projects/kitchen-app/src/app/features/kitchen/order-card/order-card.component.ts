import { Component, EventEmitter, Input, Output, OnInit, OnDestroy } from '@angular/core';
import { Order } from '@shared';

@Component({
  selector: 'app-order-card',
  template: `
    <mat-card class="order-card" [ngClass]="getTimeClass()">
      <mat-card-header>
        <mat-card-title class="order-number">{{ order.orderNumber }}</mat-card-title>
        <mat-card-subtitle>
          <mat-chip>{{ order.orderType }}</mat-chip>
          <span *ngIf="order.tableNumber"> | Table {{ order.tableNumber }}</span>
        </mat-card-subtitle>
      </mat-card-header>

      <mat-card-content>
        <div class="timer">{{ getElapsedTime() }}</div>

        <div class="items-list">
          <div *ngFor="let item of order.items" class="order-item" [class.item-done]="item.status === 'READY'">
            <mat-checkbox
              [checked]="item.status === 'READY'"
              [disabled]="order.status !== 'PREPARING' || item.status === 'READY'"
              (change)="onItemCheck(item.id)">
              <span class="qty">x{{ item.quantity }}</span>
              {{ item.menuItemName }}
            </mat-checkbox>
            <div *ngIf="item.specialRequests" class="special">
              {{ item.specialRequests }}
            </div>
          </div>
        </div>

        <div *ngIf="order.specialInstructions" class="special-instructions">
          <mat-icon inline>warning</mat-icon> {{ order.specialInstructions }}
        </div>
      </mat-card-content>

      <mat-card-actions>
        <button mat-icon-button matTooltip="Print order" (click)="printOrder()" class="print-btn">
          <mat-icon>print</mat-icon>
        </button>
        <button mat-raised-button color="primary"
                *ngIf="order.status === 'PENDING' || order.status === 'CONFIRMED'"
                (click)="statusChange.emit({ orderId: order.id, status: 'PREPARING' })">
          Start Preparing
        </button>
        <button mat-raised-button color="accent"
                *ngIf="order.status === 'PREPARING'"
                (click)="statusChange.emit({ orderId: order.id, status: 'READY' })">
          Mark Ready
        </button>
        <button mat-raised-button
                *ngIf="order.status === 'READY'"
                (click)="statusChange.emit({ orderId: order.id, status: 'SERVED' })">
          Picked Up
        </button>
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    .order-card { margin-bottom: 12px; background: #16213e; color: #eee; }
    .order-number { font-size: 20px; font-weight: bold; }
    .timer { font-size: 24px; font-weight: bold; text-align: center; margin: 8px 0; }
    .items-list { margin: 12px 0; }
    .order-item { padding: 4px 0; }
    .order-item.item-done { opacity: 0.6; }
    .order-item.item-done ::ng-deep .mat-mdc-checkbox-label { text-decoration: line-through; }
    .qty { font-weight: bold; color: #ff9800; margin-right: 4px; }
    .special { font-size: 12px; color: #ff9800; padding-left: 32px; font-style: italic; }
    .special-instructions { background: rgba(255,152,0,0.2); padding: 8px; border-radius: 4px; margin-top: 8px; font-size: 13px; }
    .time-green .timer { color: #4caf50; }
    .time-yellow .timer { color: #ff9800; }
    .time-red .timer { color: #f44336; }
    mat-card-actions { padding: 8px 16px 16px; display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
    .print-btn { margin-right: 8px; }
  `]
})
export class OrderCardComponent implements OnInit, OnDestroy {
  @Input() order!: Order;
  @Output() statusChange = new EventEmitter<{ orderId: number; status: string }>();
  @Output() itemComplete = new EventEmitter<{ orderId: number; itemId: number }>();

  private intervalId: ReturnType<typeof setInterval> | null = null;
  now = Date.now();

  ngOnInit(): void {
    this.intervalId = setInterval(() => { this.now = Date.now(); }, 1000);
  }

  ngOnDestroy(): void {
    if (this.intervalId) clearInterval(this.intervalId);
  }

  onItemCheck(itemId: number): void {
    this.itemComplete.emit({ orderId: this.order.id, itemId });
  }

  getElapsedTime(): string {
    const created = new Date(this.order.createdAt).getTime();
    const diffMs = this.now - created;
    const mins = Math.floor(diffMs / 60000);
    const secs = Math.floor((diffMs % 60000) / 1000);
    return mins >= 1 ? `${mins} min` : `${secs}s`;
  }

  getTimeClass(): string {
    const created = new Date(this.order.createdAt).getTime();
    const diff = Math.floor((this.now - created) / 60000);
    if (diff < 10) return 'time-green';
    if (diff < 20) return 'time-yellow';
    return 'time-red';
  }

  printOrder(): void {
    const w = window.open('', '_blank', 'width=400,height=500');
    if (!w) return;
    const items = (this.order.items || [])
      .map(i => `<tr><td>${i.quantity}x</td><td>${i.menuItemName}</td></tr>`)
      .join('');
    w.document.write(`
      <!DOCTYPE html><html><head><title>Order ${this.order.orderNumber}</title></head>
      <body style="font-family: sans-serif; padding: 16px;">
        <h2>Order #${this.order.orderNumber}</h2>
        <p>Type: ${this.order.orderType}${this.order.tableNumber ? ' | Table ' + this.order.tableNumber : ''}</p>
        <p>Time: ${this.getElapsedTime()}</p>
        <table border="1" cellpadding="8" style="width:100%; border-collapse: collapse;">
          <thead><tr><th>Qty</th><th>Item</th></tr></thead>
          <tbody>${items}</tbody>
        </table>
        ${this.order.specialInstructions ? '<p><strong>Note:</strong> ' + this.order.specialInstructions + '</p>' : ''}
      </body></html>`);
    w.document.close();
    w.focus();
    w.print();
    w.close();
  }
}
