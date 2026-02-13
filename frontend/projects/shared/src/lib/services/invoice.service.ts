import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Invoice } from '../models/invoice.model';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {
  private apiUrl = `${environment.apiUrl}/invoices`;

  constructor(private http: HttpClient) {}

  getInvoiceByOrder(orderId: number): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/order/${orderId}`);
  }

  downloadPdf(invoiceId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${invoiceId}/download`, { responseType: 'blob' });
  }
}
