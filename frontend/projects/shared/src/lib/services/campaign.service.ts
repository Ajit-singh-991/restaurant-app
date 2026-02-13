import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import {
  Campaign,
  CreateCampaignRequest,
  ValidatePromoResponse,
  CampaignStats
} from '../models/campaign.model';

@Injectable({ providedIn: 'root' })
export class CampaignService {
  private apiUrl = `${environment.apiUrl}/campaigns`;

  constructor(private http: HttpClient) {}

  create(request: CreateCampaignRequest): Observable<Campaign> {
    return this.http.post<Campaign>(this.apiUrl, request);
  }

  getAll(): Observable<Campaign[]> {
    return this.http.get<Campaign[]>(this.apiUrl);
  }

  getActive(): Observable<Campaign[]> {
    return this.http.get<Campaign[]>(`${this.apiUrl}/active`);
  }

  updateStatus(id: number, status: string): Observable<Campaign> {
    return this.http.patch<Campaign>(`${this.apiUrl}/${id}/status`, null, {
      params: { status }
    });
  }

  validatePromo(code: string): Observable<ValidatePromoResponse> {
    return this.http.get<ValidatePromoResponse>(`${this.apiUrl}/validate/${code}`);
  }

  redeemPromo(code: string): Observable<ValidatePromoResponse> {
    return this.http.post<ValidatePromoResponse>(`${this.apiUrl}/redeem/${code}`, null);
  }

  getStats(): Observable<CampaignStats> {
    return this.http.get<CampaignStats>(`${this.apiUrl}/stats`);
  }
}
