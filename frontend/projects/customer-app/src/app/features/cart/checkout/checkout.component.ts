import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  CartService, CartItem, OrderService, TableService,
  AuthService, RestaurantTable, CreateOrderRequest, OrderType
} from '@shared';

@Component({
  selector: 'app-checkout',
  template: `
    <div class="checkout-container">
      <h2>Checkout</h2>

      <div *ngIf="cartItems.length === 0" class="empty-redirect">
        <p>Your cart is empty.</p>
        <button mat-raised-button color="primary" routerLink="/menu">Browse Menu</button>
      </div>

      <div *ngIf="cartItems.length > 0" class="checkout-content">
        <div class="checkout-form">
          <form [formGroup]="checkoutForm" (ngSubmit)="placeOrder()">

            <!-- Order Type -->
            <mat-card class="form-section">
              <h3>Order Type</h3>
              <mat-radio-group formControlName="orderType" class="order-type-group">
                <mat-radio-button value="DINE_IN">Dine In</mat-radio-button>
                <mat-radio-button value="TAKEAWAY">Takeaway</mat-radio-button>
                <mat-radio-button value="DELIVERY">Delivery</mat-radio-button>
              </mat-radio-group>
            </mat-card>

            <!-- Table Selection (dine-in only) -->
            <mat-card *ngIf="checkoutForm.get('orderType')?.value === 'DINE_IN'" class="form-section">
              <h3>Select Table</h3>
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Table</mat-label>
                <mat-select formControlName="tableId">
                  <mat-option *ngFor="let table of availableTables" [value]="table.id">
                    Table {{ table.tableNumber }} ({{ table.section }}, seats {{ table.capacity }})
                  </mat-option>
                </mat-select>
                <mat-error *ngIf="checkoutForm.get('tableId')?.hasError('required')">
                  Please select a table
                </mat-error>
              </mat-form-field>
            </mat-card>

            <!-- Special Instructions -->
            <mat-card class="form-section">
              <h3>Special Instructions</h3>
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Any special requests?</mat-label>
                <textarea matInput formControlName="specialInstructions"
                          rows="3" maxlength="500"
                          placeholder="e.g., No onions, extra spicy..."></textarea>
                <mat-hint align="end">
                  {{ checkoutForm.get('specialInstructions')?.value?.length || 0 }}/500
                </mat-hint>
              </mat-form-field>
            </mat-card>

            <!-- Order Summary -->
            <mat-card class="form-section order-review">
              <h3>Order Review</h3>
              <div *ngFor="let item of cartItems" class="review-item">
                <span class="review-name">{{ item.name }} x{{ item.quantity }}</span>
                <span class="review-price">{{ item.subtotal | currency:'INR' }}</span>
              </div>
              <mat-divider></mat-divider>
              <div class="review-item">
                <span>Subtotal</span>
                <span>{{ subtotal | currency:'INR' }}</span>
              </div>
              <div class="review-item">
                <span>Tax (10%)</span>
                <span>{{ tax | currency:'INR' }}</span>
              </div>
              <mat-divider></mat-divider>
              <div class="review-item total">
                <span>Total</span>
                <span>{{ total | currency:'INR' }}</span>
              </div>
            </mat-card>

            <!-- Actions -->
            <div class="checkout-actions">
              <button mat-stroked-button type="button" routerLink="/cart">
                <mat-icon>arrow_back</mat-icon> Back to Cart
              </button>
              <button mat-raised-button color="primary" type="submit"
                      [disabled]="placing || checkoutForm.invalid">
                <mat-spinner *ngIf="placing" diameter="20" class="inline-spinner"></mat-spinner>
                {{ placing ? 'Placing Order...' : 'Place Order' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Success state -->
      <mat-card *ngIf="orderPlaced" class="success-card">
        <mat-icon class="success-icon">check_circle</mat-icon>
        <h2>Order Placed!</h2>
        <p>Your order <strong>{{ placedOrderNumber }}</strong> has been placed successfully.</p>
        <div class="success-actions">
          <button mat-raised-button color="primary" routerLink="/orders">
            Track Order
          </button>
          <button mat-stroked-button routerLink="/menu">
            Order More
          </button>
        </div>
      </mat-card>
    </div>
  `,
  styles: [`
    .checkout-container { max-width: 700px; margin: 24px auto; padding: 0 16px; }
    .empty-redirect { text-align: center; padding: 48px; }

    .form-section { padding: 24px; margin-bottom: 16px; }
    .form-section h3 { margin: 0 0 16px; }
    .full-width { width: 100%; }

    .order-type-group { display: flex; gap: 24px; }
    .order-type-group mat-radio-button { flex: 1; }

    .order-review .review-item { display: flex; justify-content: space-between; padding: 8px 0; }
    .review-name { color: #555; }
    .review-price { font-weight: 500; }
    .review-item.total { font-size: 18px; font-weight: 600; padding-top: 16px; }
    mat-divider { margin: 8px 0; }

    .checkout-actions { display: flex; justify-content: space-between; margin-top: 24px; gap: 16px; }
    .inline-spinner { display: inline-block; margin-right: 8px; vertical-align: middle; }

    .success-card { text-align: center; padding: 48px; }
    .success-icon { font-size: 72px; width: 72px; height: 72px; color: #4caf50; }
    .success-actions { display: flex; gap: 16px; justify-content: center; margin-top: 24px; }

    @media (max-width: 600px) {
      .order-type-group { flex-direction: column; gap: 12px; }
      .checkout-actions { flex-direction: column; }
    }
  `]
})
export class CheckoutComponent implements OnInit, OnDestroy {
  checkoutForm!: FormGroup;
  cartItems: CartItem[] = [];
  availableTables: RestaurantTable[] = [];
  subtotal = 0;
  tax = 0;
  total = 0;
  placing = false;
  orderPlaced = false;
  placedOrderNumber = '';

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private cartService: CartService,
    private orderService: OrderService,
    private tableService: TableService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.checkoutForm = this.fb.group({
      orderType: ['DINE_IN', Validators.required],
      tableId: [null],
      specialInstructions: ['']
    });

    this.cartService.cart$.pipe(takeUntil(this.destroy$)).subscribe(items => this.cartItems = items);
    this.cartService.subtotal$.pipe(takeUntil(this.destroy$)).subscribe(v => this.subtotal = v);
    this.cartService.tax$.pipe(takeUntil(this.destroy$)).subscribe(v => this.tax = v);
    this.cartService.total$.pipe(takeUntil(this.destroy$)).subscribe(v => this.total = v);

    this.tableService.getAvailableTables().subscribe(tables => this.availableTables = tables);

    // Toggle tableId required based on order type
    this.checkoutForm.get('orderType')!.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(type => {
      const tableControl = this.checkoutForm.get('tableId')!;
      if (type === 'DINE_IN') {
        tableControl.setValidators(Validators.required);
      } else {
        tableControl.clearValidators();
        tableControl.setValue(null);
      }
      tableControl.updateValueAndValidity();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  placeOrder(): void {
    if (this.checkoutForm.invalid || this.placing) return;

    if (!this.authService.isAuthenticated()) {
      this.snackBar.open('Please login to place an order', 'Login', { duration: 4000 })
        .onAction().subscribe(() => this.router.navigate(['/login'], { queryParams: { returnUrl: '/cart/checkout' } }));
      return;
    }

    this.placing = true;
    const form = this.checkoutForm.value;

    const request: CreateOrderRequest = {
      tableId: form.tableId || 0,
      orderType: form.orderType,
      specialInstructions: form.specialInstructions,
      items: this.cartItems.map(item => ({
        menuItemId: item.id,
        quantity: item.quantity,
        specialRequests: item.specialRequests
      }))
    };

    this.orderService.createOrder(request).subscribe({
      next: (order) => {
        this.placing = false;
        this.orderPlaced = true;
        this.placedOrderNumber = order.orderNumber;
        this.cartItems = [];
        this.cartService.clearCart();
      },
      error: (err) => {
        this.placing = false;
        const message = err.error?.message || 'Failed to place order. Please try again.';
        this.snackBar.open(message, 'OK', { duration: 4000 });
      }
    });
  }
}
