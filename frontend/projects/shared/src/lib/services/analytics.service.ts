import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardStats, SalesReport, MenuAnalytics, ReportType, ExportFormat } from '../models/analytics.model';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private apiUrl = `${environment.apiUrl}/analytics`;

  constructor(private http: HttpClient) {}

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/dashboard`);
  }

  getSalesReport(start: string, end: string): Observable<SalesReport> {
    const params = new HttpParams().set('start', start).set('end', end);
    return this.http.get<SalesReport>(`${this.apiUrl}/sales`, { params });
  }

  getMenuAnalytics(): Observable<MenuAnalytics> {
    return this.http.get<MenuAnalytics>(`${this.apiUrl}/menu`);
  }

  exportReport(type: ReportType, format: ExportFormat, start: string, end: string): Observable<Blob> {
    const params = new HttpParams()
      .set('type', type)
      .set('format', format)
      .set('start', start)
      .set('end', end);
    return this.http.get(`${this.apiUrl}/export`, { params, responseType: 'blob' });
  }
}
