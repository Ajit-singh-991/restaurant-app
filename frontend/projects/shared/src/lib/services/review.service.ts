import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Review, CreateReviewRequest, ItemRatingSummary, ReviewStats } from '../models/review.model';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private apiUrl = `${environment.apiUrl}/reviews`;

  constructor(private http: HttpClient) {}

  createReview(request: CreateReviewRequest): Observable<Review> {
    return this.http.post<Review>(this.apiUrl, request);
  }

  getReviewsByItem(menuItemId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiUrl}/item/${menuItemId}`);
  }

  getItemRating(menuItemId: number): Observable<ItemRatingSummary> {
    return this.http.get<ItemRatingSummary>(`${this.apiUrl}/item/${menuItemId}/rating`);
  }

  getMyReviews(): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiUrl}/my`);
  }

  getReviewsByOrder(orderId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiUrl}/order/${orderId}`);
  }

  getAllReviews(): Observable<Review[]> {
    return this.http.get<Review[]>(this.apiUrl);
  }

  getUnansweredReviews(): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiUrl}/unanswered`);
  }

  getReviewStats(): Observable<ReviewStats> {
    return this.http.get<ReviewStats>(`${this.apiUrl}/stats`);
  }

  respondToReview(reviewId: number, response: string): Observable<Review> {
    return this.http.post<Review>(`${this.apiUrl}/${reviewId}/respond`, { response });
  }

  deleteReview(reviewId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${reviewId}`);
  }
}
