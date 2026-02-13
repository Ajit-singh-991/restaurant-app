import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TimeEntry, StaffSummary } from '../models/staff.model';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class StaffService {
  private apiUrl = `${environment.apiUrl}/staff`;

  constructor(private http: HttpClient) {}

  clockIn(): Observable<TimeEntry> {
    return this.http.post<TimeEntry>(`${this.apiUrl}/clock-in`, null);
  }

  clockOut(): Observable<TimeEntry> {
    return this.http.post<TimeEntry>(`${this.apiUrl}/clock-out`, null);
  }

  getActiveSession(): Observable<TimeEntry> {
    return this.http.get<TimeEntry>(`${this.apiUrl}/my/session`);
  }

  getMyHistory(): Observable<TimeEntry[]> {
    return this.http.get<TimeEntry[]>(`${this.apiUrl}/my/history`);
  }

  addTip(entryId: number, amount: number): Observable<TimeEntry> {
    return this.http.post<TimeEntry>(`${this.apiUrl}/time-entries/${entryId}/tip`, { amount });
  }

  getActiveSessions(): Observable<TimeEntry[]> {
    return this.http.get<TimeEntry[]>(`${this.apiUrl}/active-sessions`);
  }

  getDailySummary(date?: string): Observable<StaffSummary> {
    if (date) {
      return this.http.get<StaffSummary>(`${this.apiUrl}/summary`, { params: { date } });
    }
    return this.http.get<StaffSummary>(`${this.apiUrl}/summary`);
  }
}
