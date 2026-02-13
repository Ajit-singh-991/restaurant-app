import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CartService, CartItem } from '@shared';

@Component({
  selector: 'app-cart-view',
  template: `
    <div class="cart-container">
      <h2>Your Cart</h2>

      <!-- Empty state -->
      <mat-card *ngIf="cartItems.length === 0" class="empty-cart">
        <mat-icon>shopping_cart</mat-icon>
        <p>Your cart is empty</p>
        <button mat-raised-button color="primary" routerLink="/menu">Browse Menu</button>
      </mat-card>

      <!-- Cart content -->
      <div *ngIf="cartItems.length > 0" class="cart-content">
        <div class="cart-items">
          <mat-card *ngFor="let item of cartItems" class="cart-item">
            <div class="item-row">
              <img [src]="item.imageUrl || 'assets/images/default-food.jpg'"
                   [alt]="item.name" class="item-image">

              <div class="item-details">
                <h3>{{ item.name }}</h3>
                <p class="item-price">{{ item.price | currency:'INR' }} each</p>
                <div *ngIf="item.specialRequests" class="special-requests">
                  <mat-icon inline>note</mat-icon> {{ item.specialRequests }}
                </div>
              </div>

              <div class="quantity-controls">
                <button mat-icon-button color="primary" (click)="decreaseQuantity(item)">
                  <mat-icon>remove_circle_outline</mat-icon>
                </button>
                <span class="quantity">{{ item.quantity }}</span>
                <button mat-icon-button color="primary" (click)="increaseQuantity(item)">
                  <mat-icon>add_circle_outline</mat-icon>
                </button>
              </div>

              <div class="item-subtotal">
                <span>{{ item.subtotal | currency:'INR' }}</span>
              </div>

              <button mat-icon-button color="warn" (click)="removeItem(item)"
                      matTooltip="Remove item">
                <mat-icon>delete</mat-icon>
              </button>
            </div>
          </mat-card>
        </div>

        <!-- Cart summary -->
        <mat-card class="cart-summary">
          <h3>Order Summary</h3>
          <div class="summary-row">
            <span>Subtotal</span>
            <span>{{ subtotal | currency:'INR' }}</span>
          </div>
          <div class="summary-row">
            <span>Tax (10%)</span>
            <span>{{ tax | currency:'INR' }}</span>
          </div>
          <mat-divider></mat-divider>
          <div class="summary-row total">
            <span>Total</span>
            <span>{{ total | currency:'INR' }}</span>
          </div>
          <div class="summary-actions">
            <button mat-stroked-button color="warn" (click)="clearCart()">
              Clear Cart
            </button>
            <button mat-raised-button color="primary" (click)="proceedToCheckout()">
              Proceed to Checkout
            </button>
          </div>
          <button mat-button routerLink="/menu" class="continue-shopping">
            <mat-icon>arrow_back</mat-icon> Continue Shopping
          </button>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .cart-container { max-width: 1000px; margin: 24px auto; padding: 0 16px; }
    .empty-cart { text-align: center; padding: 48px; }
    .empty-cart mat-icon { font-size: 64px; width: 64px; height: 64px; color: #ccc; }
    .empty-cart p { color: #666; margin: 16px 0; }

    .cart-content { display: grid; grid-template-columns: 1fr 320px; gap: 24px; align-items: start; }
    .cart-items { display: flex; flex-direction: column; gap: 12px; }

    .cart-item { padding: 16px; }
    .item-row { display: flex; align-items: center; gap: 16px; }
    .item-image { width: 72px; height: 72px; object-fit: cover; border-radius: 8px; }
    .item-details { flex: 1; min-width: 0; }
    .item-details h3 { margin: 0 0 4px; font-size: 16px; }
    .item-price { margin: 0; color: #666; font-size: 14px; }
    .special-requests { font-size: 12px; color: #888; margin-top: 4px; }
    .special-requests mat-icon { font-size: 14px; vertical-align: middle; }

    .quantity-controls { display: flex; align-items: center; gap: 4px; }
    .quantity { font-size: 16px; font-weight: 500; min-width: 28px; text-align: center; }

    .item-subtotal { font-weight: 500; min-width: 80px; text-align: right; }

    .cart-summary { padding: 24px; position: sticky; top: 80px; }
    .cart-summary h3 { margin: 0 0 16px; }
    .summary-row { display: flex; justify-content: space-between; padding: 8px 0; }
    .summary-row.total { font-size: 18px; font-weight: 600; padding-top: 16px; }
    mat-divider { margin: 8px 0; }

    .summary-actions { display: flex; gap: 12px; margin-top: 24px; }
    .summary-actions button { flex: 1; }
    .continue-shopping { width: 100%; margin-top: 12px; }

    @media (max-width: 768px) {
      .cart-content { grid-template-columns: 1fr; }
      .item-row { flex-wrap: wrap; }
      .item-image { width: 56px; height: 56px; }
      .cart-summary { position: static; }
    }
  `]
})
export class CartViewComponent implements OnInit, OnDestroy {
  cartItems: CartItem[] = [];
  subtotal = 0;
  tax = 0;
  total = 0;

  private destroy$ = new Subject<void>();

  constructor(
    private cartService: CartService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.cartService.cart$.pipe(takeUntil(this.destroy$)).subscribe(items => this.cartItems = items);
    this.cartService.subtotal$.pipe(takeUntil(this.destroy$)).subscribe(v => this.subtotal = v);
    this.cartService.tax$.pipe(takeUntil(this.destroy$)).subscribe(v => this.tax = v);
    this.cartService.total$.pipe(takeUntil(this.destroy$)).subscribe(v => this.total = v);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  increaseQuantity(item: CartItem): void {
    this.cartService.updateQuantity(item.id, item.quantity + 1);
  }

  decreaseQuantity(item: CartItem): void {
    this.cartService.updateQuantity(item.id, item.quantity - 1);
  }

  removeItem(item: CartItem): void {
    this.cartService.removeFromCart(item.id);
    this.snackBar.open(`${item.name} removed from cart`, 'OK', { duration: 2000 });
  }

  clearCart(): void {
    this.cartService.clearCart();
    this.snackBar.open('Cart cleared', 'OK', { duration: 2000 });
  }

  proceedToCheckout(): void {
    this.router.navigate(['/cart/checkout']);
  }
}
