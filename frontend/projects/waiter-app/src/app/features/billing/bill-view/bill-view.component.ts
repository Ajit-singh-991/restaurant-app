import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { OrderService, PaymentService, Order, Payment } from '@shared';
import { PaymentDialogComponent } from '../payment-dialog/payment-dialog.component';
import { SplitBillDialogComponent } from '../split-bill-dialog/split-bill-dialog.component';

@Component({
  selector: 'app-bill-view',
  template: `
    <div class="bill-container" *ngIf="order">
      <div class="bill-header">
        <button mat-icon-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h2>Bill - {{ order.orderNumber }}</h2>
      </div>

      <mat-card class="bill-card">
        <!-- Order info -->
        <div class="bill-info">
          <div class="info-row">
            <span>Order #</span>
            <strong>{{ order.orderNumber }}</strong>
          </div>
          <div class="info-row" *ngIf="order.tableNumber">
            <span>Table</span>
            <strong>{{ order.tableNumber }}</strong>
          </div>
          <div class="info-row">
            <span>Type</span>
            <mat-chip>{{ order.orderType }}</mat-chip>
          </div>
          <div class="info-row">
            <span>Date</span>
            <span>{{ order.createdAt | date:'medium' }}</span>
          </div>
        </div>

        <mat-divider></mat-divider>

        <!-- Itemized list -->
        <h3>Items</h3>
        <div class="items-table">
          <div class="items-header">
            <span class="col-name">Item</span>
            <span class="col-qty">Qty</span>
            <span class="col-price">Rate</span>
            <span class="col-total">Amount</span>
          </div>
          <div *ngFor="let item of order.items" class="items-row">
            <span class="col-name">{{ item.menuItemName }}</span>
            <span class="col-qty">{{ item.quantity }}</span>
            <span class="col-price">{{ item.unitPrice | currency:'INR' }}</span>
            <span class="col-total">{{ item.totalPrice | currency:'INR' }}</span>
          </div>
        </div>

        <mat-divider></mat-divider>

        <!-- Totals -->
        <div class="totals">
          <div class="total-row">
            <span>Subtotal</span>
            <span>{{ order.subtotal | currency:'INR' }}</span>
          </div>
          <div class="total-row">
            <span>Tax (10%)</span>
            <span>{{ order.taxAmount | currency:'INR' }}</span>
          </div>
          <mat-divider></mat-divider>
          <div class="total-row grand-total">
            <span>Grand Total</span>
            <span>{{ order.totalAmount | currency:'INR' }}</span>
          </div>
        </div>

        <mat-divider></mat-divider>

        <!-- Payment status -->
        <div class="payment-status">
          <ng-container *ngIf="payment; else unpaid">
            <div class="paid-badge">
              <mat-icon>check_circle</mat-icon>
              <span>Paid via {{ payment.paymentMethod }}</span>
            </div>
            <p class="payment-time">{{ payment.createdAt | date:'medium' }}</p>
            <p *ngIf="payment.transactionId" class="txn-id">Txn: {{ payment.transactionId }}</p>
          </ng-container>
          <ng-template #unpaid>
            <div class="unpaid-badge">
              <mat-icon>pending</mat-icon>
              <span>Payment Pending</span>
            </div>
          </ng-template>
        </div>

        <!-- Actions -->
        <div class="bill-actions">
          <button mat-raised-button color="primary"
                  *ngIf="!payment"
                  (click)="openPaymentDialog()">
            <mat-icon>payment</mat-icon> Process Payment
          </button>
          <button mat-stroked-button *ngIf="payment" [routerLink]="['/billing', order.id, 'invoice']">
            <mat-icon>receipt</mat-icon> View Invoice
          </button>
          <button mat-stroked-button (click)="openSplitBillDialog()" *ngIf="!payment">
            <mat-icon>call_split</mat-icon> Split Bill
          </button>
          <button mat-stroked-button (click)="printBill()">
            <mat-icon>print</mat-icon> Print
          </button>
        </div>
      </mat-card>
    </div>

    <div *ngIf="loading" class="loading">
      <mat-spinner diameter="40"></mat-spinner>
    </div>
  `,
  styles: [`
    .bill-container { max-width: 600px; margin: 24px auto; padding: 0 16px; }
    .bill-header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
    .bill-header h2 { margin: 0; }
    .bill-card { padding: 24px; }

    .bill-info { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 16px; }
    .info-row { display: flex; justify-content: space-between; }
    .info-row span:first-child { color: #666; }

    h3 { margin: 16px 0 8px; }

    .items-table { margin-bottom: 16px; }
    .items-header, .items-row { display: grid; grid-template-columns: 2fr 1fr 1fr 1fr; padding: 8px 0; }
    .items-header { font-weight: 500; color: #666; border-bottom: 1px solid #eee; }
    .col-qty, .col-price, .col-total { text-align: right; }

    .totals { margin: 16px 0; }
    .total-row { display: flex; justify-content: space-between; padding: 8px 0; }
    .grand-total { font-size: 20px; font-weight: 600; padding-top: 16px; }

    .payment-status { text-align: center; padding: 16px 0; }
    .paid-badge { display: flex; align-items: center; justify-content: center; gap: 8px; color: #4caf50; font-weight: 500; font-size: 16px; }
    .unpaid-badge { display: flex; align-items: center; justify-content: center; gap: 8px; color: #ff9800; font-weight: 500; font-size: 16px; }
    .payment-time { color: #666; margin: 4px 0 0; }
    .txn-id { color: #888; font-size: 12px; margin: 2px 0 0; }

    .bill-actions { display: flex; gap: 12px; justify-content: center; margin-top: 16px; }

    .loading { display: flex; justify-content: center; padding: 48px; }

    @media (max-width: 600px) {
      .bill-info { grid-template-columns: 1fr; }
    }
  `]
})
export class BillViewComponent implements OnInit {
  order: Order | null = null;
  payment: Payment | null = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private paymentService: PaymentService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const orderId = Number(this.route.snapshot.paramMap.get('orderId'));
    this.orderService.getOrder(orderId).subscribe({
      next: order => {
        this.order = order;
        this.loading = false;
        this.loadPayment(orderId);
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Order not found', 'OK', { duration: 3000 });
        this.router.navigate(['/orders']);
      }
    });
  }

  loadPayment(orderId: number): void {
    this.paymentService.getPaymentByOrder(orderId).subscribe({
      next: payment => this.payment = payment,
      error: () => {} // No payment yet — that's fine
    });
  }

  openPaymentDialog(): void {
    if (!this.order) return;
    const dialogRef = this.dialog.open(PaymentDialogComponent, {
      width: '400px',
      data: { order: this.order }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.payment = result;
        this.snackBar.open('Payment processed successfully', 'OK', { duration: 3000 });
      }
    });
  }

  printBill(): void {
    window.print();
  }

  openSplitBillDialog(): void {
    if (!this.order) return;
    this.dialog.open(SplitBillDialogComponent, { width: '360px', data: { order: this.order } });
  }

  goBack(): void {
    this.router.navigate(['/orders']);
  }
}
