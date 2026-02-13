import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { PaymentService, Order, PaymentRequest } from '@shared';

@Component({
  selector: 'app-payment-dialog',
  template: `
    <h2 mat-dialog-title>Process Payment</h2>
    <form [formGroup]="paymentForm" (ngSubmit)="submit()">
      <mat-dialog-content>
        <div class="amount-display">
          <span class="amount-label">Amount Due</span>
          <span class="amount-value">{{ data.order.totalAmount | currency:'INR' }}</span>
        </div>

        <h3>Payment Method</h3>
        <mat-radio-group formControlName="paymentMethod" class="method-group">
          <mat-radio-button value="CASH">
            <mat-icon>payments</mat-icon> Cash
          </mat-radio-button>
          <mat-radio-button value="CARD">
            <mat-icon>credit_card</mat-icon> Card
          </mat-radio-button>
          <mat-radio-button value="UPI">
            <mat-icon>qr_code</mat-icon> UPI
          </mat-radio-button>
          <mat-radio-button value="WALLET">
            <mat-icon>account_balance_wallet</mat-icon> Wallet
          </mat-radio-button>
        </mat-radio-group>

        <mat-form-field appearance="outline" class="full-width"
                        *ngIf="showTransactionId">
          <mat-label>Transaction ID</mat-label>
          <input matInput formControlName="transactionId" placeholder="Enter transaction reference">
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Email receipt (optional)</mat-label>
          <input matInput type="email" formControlName="recipientEmail" placeholder="customer@example.com">
        </mat-form-field>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button mat-dialog-close type="button">Cancel</button>
        <button mat-raised-button color="primary" type="submit"
                [disabled]="processing || paymentForm.invalid">
          <mat-spinner *ngIf="processing" diameter="18" class="inline-spinner"></mat-spinner>
          {{ processing ? 'Processing...' : 'Confirm Payment' }}
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [`
    .amount-display { text-align: center; padding: 16px; background: #f5f5f5; border-radius: 8px; margin-bottom: 16px; }
    .amount-label { display: block; color: #666; font-size: 14px; }
    .amount-value { display: block; font-size: 28px; font-weight: 600; color: #1976d2; }
    h3 { margin: 16px 0 8px; }
    .method-group { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
    .method-group mat-radio-button { padding: 8px; }
    .method-group mat-icon { vertical-align: middle; margin-right: 4px; font-size: 20px; }
    .full-width { width: 100%; margin-top: 16px; }
    .inline-spinner { display: inline-block; margin-right: 8px; vertical-align: middle; }
  `]
})
export class PaymentDialogComponent {
  paymentForm: FormGroup;
  processing = false;

  get showTransactionId(): boolean {
    const method = this.paymentForm.get('paymentMethod')?.value;
    return method === 'CARD' || method === 'UPI' || method === 'WALLET';
  }

  constructor(
    private fb: FormBuilder,
    private paymentService: PaymentService,
    private dialogRef: MatDialogRef<PaymentDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { order: Order }
  ) {
    this.paymentForm = this.fb.group({
      paymentMethod: ['CASH', Validators.required],
      transactionId: [''],
      recipientEmail: ['']
    });
  }

  submit(): void {
    if (this.paymentForm.invalid || this.processing) return;

    this.processing = true;
    const form = this.paymentForm.value;

    const request: PaymentRequest = {
      orderId: this.data.order.id,
      paymentMethod: form.paymentMethod,
      transactionId: form.transactionId || undefined,
      recipientEmail: form.recipientEmail || undefined
    };

    this.paymentService.processPayment(request).subscribe({
      next: payment => {
        this.processing = false;
        this.dialogRef.close(payment);
      },
      error: () => {
        this.processing = false;
      }
    });
  }
}
