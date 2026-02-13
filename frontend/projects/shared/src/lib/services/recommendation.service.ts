import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import {
  PersonalizedRecommendations,
  TrendingItems,
  PairingSuggestion
} from '../models/recommendation.model';

@Injectable({ providedIn: 'root' })
export class RecommendationService {
  private apiUrl = `${environment.apiUrl}/recommendations`;

  constructor(private http: HttpClient) {}

  getPersonalized(): Observable<PersonalizedRecommendations> {
    return this.http.get<PersonalizedRecommendations>(`${this.apiUrl}/personalized`);
  }

  getTrending(): Observable<TrendingItems> {
    return this.http.get<TrendingItems>(`${this.apiUrl}/trending`);
  }

  getPairingSuggestions(itemId: number): Observable<PairingSuggestion> {
    return this.http.get<PairingSuggestion>(`${this.apiUrl}/pairing/${itemId}`);
  }
}
