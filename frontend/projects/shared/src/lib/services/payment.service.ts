import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Payment, PaymentRequest, PaymentStats } from '../models/payment.model';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = `${environment.apiUrl}/payments`;

  constructor(private http: HttpClient) {}

  processPayment(request: PaymentRequest): Observable<Payment> {
    return this.http.post<Payment>(`${this.apiUrl}/process`, request);
  }

  getPaymentByOrder(orderId: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.apiUrl}/order/${orderId}`);
  }

  getAllPayments(): Observable<Payment[]> {
    return this.http.get<Payment[]>(this.apiUrl);
  }

  refundPayment(id: number, reason: string): Observable<Payment> {
    return this.http.post<Payment>(`${this.apiUrl}/${id}/refund`, { reason });
  }

  getStats(): Observable<PaymentStats> {
    return this.http.get<PaymentStats>(`${this.apiUrl}/stats`);
  }
}
