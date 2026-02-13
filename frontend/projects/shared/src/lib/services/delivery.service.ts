import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import {
  DeliveryTracking,
  CreateDeliveryRequest,
  AssignDriverRequest,
  DeliveryStats,
  DeliveryStatus
} from '../models/delivery.model';

@Injectable({ providedIn: 'root' })
export class DeliveryService {
  private apiUrl = `${environment.apiUrl}/delivery`;

  constructor(private http: HttpClient) {}

  createDelivery(request: CreateDeliveryRequest): Observable<DeliveryTracking> {
    return this.http.post<DeliveryTracking>(this.apiUrl, request);
  }

  getByOrderId(orderId: number): Observable<DeliveryTracking> {
    return this.http.get<DeliveryTracking>(`${this.apiUrl}/order/${orderId}`);
  }

  assignDriver(deliveryId: number, request: AssignDriverRequest): Observable<DeliveryTracking> {
    return this.http.post<DeliveryTracking>(`${this.apiUrl}/${deliveryId}/assign`, request);
  }

  updateStatus(deliveryId: number, status: DeliveryStatus): Observable<DeliveryTracking> {
    return this.http.patch<DeliveryTracking>(`${this.apiUrl}/${deliveryId}/status`, null, {
      params: { status }
    });
  }

  getActiveDeliveries(): Observable<DeliveryTracking[]> {
    return this.http.get<DeliveryTracking[]>(`${this.apiUrl}/active`);
  }

  getPendingDeliveries(): Observable<DeliveryTracking[]> {
    return this.http.get<DeliveryTracking[]>(`${this.apiUrl}/pending`);
  }

  getMyDeliveries(): Observable<DeliveryTracking[]> {
    return this.http.get<DeliveryTracking[]>(`${this.apiUrl}/my`);
  }

  getStats(): Observable<DeliveryStats> {
    return this.http.get<DeliveryStats>(`${this.apiUrl}/stats`);
  }
}
