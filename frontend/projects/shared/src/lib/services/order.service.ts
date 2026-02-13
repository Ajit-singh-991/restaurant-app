import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order, CreateOrderRequest, OrderStatus } from '../models/order.model';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = `${environment.apiUrl}/orders`;

  constructor(private http: HttpClient) {}

  createOrder(request: CreateOrderRequest): Observable<Order> {
    return this.http.post<Order>(this.apiUrl, request);
  }

  getOrder(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/${id}`);
  }

  getActiveOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/active`);
  }

  getActiveTableOrders(tableId: number): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/table/${tableId}/active`);
  }

  getOrdersByCustomer(customerId: number): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/customer/${customerId}`);
  }

  getOrdersByStatus(status: OrderStatus): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/status/${status}`);
  }

  updateOrderStatus(id: number, status: OrderStatus): Observable<Order> {
    return this.http.put<Order>(`${this.apiUrl}/${id}/status`, null, { params: { status } });
  }

  calculateSplit(orderId: number, request: SplitRequest): Observable<SplitResponse> {
    return this.http.post<SplitResponse>(`${this.apiUrl}/${orderId}/split`, request);
  }
}

export interface SplitRequest {
  numberOfWays?: number;
  percentages?: number[];
  personItemIds?: number[][];
}

export interface SplitResponse {
  orderTotal: number;
  amountsPerPerson: number[];
}
