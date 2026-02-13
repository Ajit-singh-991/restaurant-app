import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class QrCodeService {
  private apiUrl = `${environment.apiUrl}/qr`;

  constructor(private http: HttpClient) {}

  getTableQrCode(tableId: number, width = 300, height = 300): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/table/${tableId}`, {
      params: { width: width.toString(), height: height.toString() },
      responseType: 'blob'
    });
  }

  getTableOrderUrl(tableId: number): Observable<{ url: string }> {
    return this.http.get<{ url: string }>(`${this.apiUrl}/table/${tableId}/url`);
  }
}
