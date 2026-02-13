import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { OrderService, InvoiceService, Invoice, Order } from '@shared';

@Component({
  selector: 'app-invoice-view',
  template: `
    <div class="invoice-container" *ngIf="invoice && order">
      <div class="invoice-header">
        <button mat-icon-button (click)="goBack()"><mat-icon>arrow_back</mat-icon></button>
        <h2>Invoice {{ invoice.invoiceNumber }}</h2>
      </div>
      <mat-card class="invoice-card">
        <p><strong>Order #</strong> {{ order.orderNumber }}</p>
        <p><strong>Date</strong> {{ invoice.generatedAt | date:'medium' }}</p>
        <p><strong>Bill To</strong> {{ invoice.billToName }}</p>
        <p *ngIf="invoice.billToContact"><strong>Contact</strong> {{ invoice.billToContact }}</p>
        <mat-divider></mat-divider>
        <div class="totals">
          <p>Subtotal: {{ invoice.subtotal | currency:'INR' }}</p>
          <p>Tax: {{ invoice.taxAmount | currency:'INR' }}</p>
          <p><strong>Total: {{ invoice.totalAmount | currency:'INR' }}</strong></p>
        </div>
        <mat-divider></mat-divider>
        <div class="actions">
          <button mat-raised-button (click)="print()"><mat-icon>print</mat-icon> Print</button>
          <button mat-raised-button color="primary" (click)="downloadPdf()">
            <mat-icon>download</mat-icon> Download PDF
          </button>
        </div>
      </mat-card>
    </div>
    <div *ngIf="loading" class="loading"><mat-spinner diameter="40"></mat-spinner></div>
    <div *ngIf="!loading && !invoice" class="loading">Invoice not found</div>
  `,
  styles: [`
    .invoice-container { max-width: 600px; margin: 24px auto; padding: 16px; }
    .invoice-header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
    .invoice-header h2 { margin: 0; }
    .invoice-card { padding: 24px; }
    .totals { margin: 16px 0; }
    .actions { display: flex; gap: 12px; margin-top: 16px; }
    .loading { text-align: center; padding: 48px; }
  `]
})
export class InvoiceViewComponent implements OnInit {
  invoice: Invoice | null = null;
  order: Order | null = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private invoiceService: InvoiceService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const orderId = Number(this.route.snapshot.paramMap.get('orderId'));
    this.invoiceService.getInvoiceByOrder(orderId).subscribe({
      next: inv => {
        this.invoice = inv;
        this.orderService.getOrder(orderId).subscribe(o => this.order = o);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Invoice not found', 'OK');
      }
    });
  }

  print(): void {
    window.print();
  }

  downloadPdf(): void {
    if (!this.invoice) return;
    this.invoiceService.downloadPdf(this.invoice.id).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `invoice-${this.invoice!.invoiceNumber}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
      this.snackBar.open('Download started', 'OK', { duration: 2000 });
    });
  }

  goBack(): void {
    this.router.navigate(['/billing', this.route.snapshot.paramMap.get('orderId')]);
  }
}
