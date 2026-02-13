import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import {
  SalesForecast,
  CustomerSegmentation,
  MenuOptimization,
  PeakHoursAnalysis
} from '../models/advanced-analytics.model';

@Injectable({ providedIn: 'root' })
export class AdvancedAnalyticsService {
  private apiUrl = `${environment.apiUrl}/analytics/advanced`;

  constructor(private http: HttpClient) {}

  getSalesForecast(days: number = 7): Observable<SalesForecast> {
    return this.http.get<SalesForecast>(`${this.apiUrl}/forecast`, {
      params: { days: days.toString() }
    });
  }

  getCustomerSegmentation(): Observable<CustomerSegmentation> {
    return this.http.get<CustomerSegmentation>(`${this.apiUrl}/customers/segments`);
  }

  getMenuOptimization(): Observable<MenuOptimization> {
    return this.http.get<MenuOptimization>(`${this.apiUrl}/menu/optimization`);
  }

  getPeakHours(): Observable<PeakHoursAnalysis> {
    return this.http.get<PeakHoursAnalysis>(`${this.apiUrl}/peak-hours`);
  }
}
