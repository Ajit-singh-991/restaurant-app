import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoyaltyAccount, LoyaltyTransaction } from '../models/loyalty.model';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LoyaltyService {
  private apiUrl = `${environment.apiUrl}/loyalty`;

  constructor(private http: HttpClient) {}

  getMyAccount(): Observable<LoyaltyAccount> {
    return this.http.get<LoyaltyAccount>(`${this.apiUrl}/my`);
  }

  getMyTransactions(): Observable<LoyaltyTransaction[]> {
    return this.http.get<LoyaltyTransaction[]>(`${this.apiUrl}/my/transactions`);
  }

  redeemPoints(points: number): Observable<LoyaltyAccount> {
    return this.http.post<LoyaltyAccount>(`${this.apiUrl}/redeem`, { points });
  }

  getAllAccounts(): Observable<LoyaltyAccount[]> {
    return this.http.get<LoyaltyAccount[]>(`${this.apiUrl}/accounts`);
  }
}
