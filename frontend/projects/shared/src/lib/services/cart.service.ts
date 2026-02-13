import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, map } from 'rxjs';
import { MenuItem, CartItem } from '../models/menu.model';

const STORAGE_KEY = 'restaurant_cart';
const TAX_RATE = 0.10;

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private cartSubject = new BehaviorSubject<CartItem[]>(this.loadCart());
  public cart$ = this.cartSubject.asObservable();

  public itemCount$: Observable<number> = this.cart$.pipe(
    map(items => items.reduce((sum, i) => sum + i.quantity, 0))
  );

  public subtotal$: Observable<number> = this.cart$.pipe(
    map(items => items.reduce((sum, i) => sum + i.subtotal, 0))
  );

  public tax$: Observable<number> = this.subtotal$.pipe(
    map(subtotal => subtotal * TAX_RATE)
  );

  public total$: Observable<number> = this.subtotal$.pipe(
    map(subtotal => subtotal + subtotal * TAX_RATE)
  );

  addToCart(item: MenuItem, quantity: number = 1, specialRequests?: string): void {
    const cart = this.cartSubject.value;
    const existing = cart.find(c => c.id === item.id);

    if (existing) {
      existing.quantity += quantity;
      existing.subtotal = existing.quantity * existing.price;
      if (specialRequests) existing.specialRequests = specialRequests;
    } else {
      cart.push({
        ...item,
        quantity,
        subtotal: item.price * quantity,
        specialRequests
      });
    }

    this.update(cart);
  }

  removeFromCart(itemId: number): void {
    const cart = this.cartSubject.value.filter(i => i.id !== itemId);
    this.update(cart);
  }

  updateQuantity(itemId: number, quantity: number): void {
    if (quantity < 1) {
      this.removeFromCart(itemId);
      return;
    }
    const cart = this.cartSubject.value;
    const item = cart.find(i => i.id === itemId);
    if (item) {
      item.quantity = quantity;
      item.subtotal = item.price * quantity;
      this.update(cart);
    }
  }

  updateSpecialRequests(itemId: number, specialRequests: string): void {
    const cart = this.cartSubject.value;
    const item = cart.find(i => i.id === itemId);
    if (item) {
      item.specialRequests = specialRequests;
      this.update(cart);
    }
  }

  clearCart(): void {
    this.update([]);
  }

  getCart(): CartItem[] {
    return this.cartSubject.value;
  }

  isEmpty(): boolean {
    return this.cartSubject.value.length === 0;
  }

  private update(cart: CartItem[]): void {
    this.cartSubject.next([...cart]);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(cart));
  }

  private loadCart(): CartItem[] {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (!stored) return [];
    try {
      return JSON.parse(stored);
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return [];
    }
  }
}
