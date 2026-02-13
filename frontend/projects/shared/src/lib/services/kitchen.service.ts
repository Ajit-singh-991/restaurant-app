import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order } from '../models/order.model';
import { KitchenStation } from '../models/menu.model';
import { environment } from '@environments/environment';

export interface KitchenStats {
  activeOrders: number;
  preparingOrders: number;
  readyOrders: number;
  avgPrepTimeMinutes: number;
}

@Injectable({
  providedIn: 'root'
})
export class KitchenService {
  private apiUrl = `${environment.apiUrl}/kitchen`;

  constructor(private http: HttpClient) {}

  getStations(): Observable<KitchenStation[]> {
    return this.http.get<KitchenStation[]>(`${this.apiUrl}/stations`);
  }

  getActiveOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/orders/active`);
  }

  getActiveOrdersByStation(stationId: number): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/orders/station/${stationId}`);
  }

  startPreparing(orderId: number): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/orders/${orderId}/start`, null);
  }

  markReady(orderId: number): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/orders/${orderId}/ready`, null);
  }

  markItemComplete(orderId: number, itemId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/orders/${orderId}/items/${itemId}/complete`, null);
  }

  getStats(): Observable<KitchenStats> {
    return this.http.get<KitchenStats>(`${this.apiUrl}/stats`);
  }
}
