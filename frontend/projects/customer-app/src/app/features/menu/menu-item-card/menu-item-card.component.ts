import { Component, Input } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MenuItem, CartService } from '@shared';

@Component({
  selector: 'app-menu-item-card',
  template: `
    <mat-card class="item-card" [class.unavailable]="!item.available">
      <img mat-card-image
           [src]="item.imageUrl || 'assets/images/default-food.jpg'"
           [alt]="item.name"
           class="item-image">

      <mat-card-header>
        <mat-card-title>{{ item.name }}</mat-card-title>
        <mat-card-subtitle>
          <span class="price">{{ item.price | currency:'INR' }}</span>
          <span *ngIf="item.preparationTimeMinutes" class="prep-time">
            <mat-icon inline>schedule</mat-icon> {{ item.preparationTimeMinutes }} min
          </span>
        </mat-card-subtitle>
      </mat-card-header>

      <mat-card-content>
        <p class="description">{{ item.description }}</p>
        <div class="tags">
          <mat-chip-set>
            <mat-chip *ngIf="item.vegetarian" class="veg-chip">Veg</mat-chip>
            <mat-chip *ngIf="item.vegan">Vegan</mat-chip>
            <mat-chip *ngIf="item.glutenFree">GF</mat-chip>
          </mat-chip-set>
        </div>
      </mat-card-content>

      <mat-card-actions>
        <button mat-raised-button color="primary"
                *ngIf="item.available; else unavailableTpl"
                (click)="addToCart()">
          <mat-icon>add_shopping_cart</mat-icon>
          Add to Cart
        </button>
        <ng-template #unavailableTpl>
          <button mat-raised-button disabled>Unavailable</button>
        </ng-template>
      </mat-card-actions>

      <div *ngIf="!item.available" class="unavailable-overlay">
        <span>Currently Unavailable</span>
      </div>
    </mat-card>
  `,
  styles: [`
    .item-card { position: relative; height: 100%; display: flex; flex-direction: column; }
    .item-image { height: 180px; object-fit: cover; }
    .price { font-size: 18px; font-weight: 500; color: #e91e63; }
    .prep-time { margin-left: 8px; font-size: 12px; color: #666; }
    .prep-time mat-icon { font-size: 14px; vertical-align: middle; }
    .description { color: #555; font-size: 14px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
    .tags { margin-top: 8px; }
    .veg-chip { background-color: #4caf50 !important; color: white !important; }
    mat-card-actions { margin-top: auto; padding: 8px 16px 16px; }
    .unavailable { opacity: 0.7; }
    .unavailable-overlay {
      position: absolute; top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center;
      color: white; font-weight: 500; font-size: 16px; border-radius: 4px;
    }
  `]
})
export class MenuItemCardComponent {
  @Input() item!: MenuItem;

  constructor(
    private snackBar: MatSnackBar,
    private cartService: CartService
  ) {}

  addToCart(): void {
    this.cartService.addToCart(this.item);
    this.snackBar.open(`${this.item.name} added to cart`, 'OK', {
      duration: 2000,
      panelClass: 'success-snackbar'
    });
  }
}
