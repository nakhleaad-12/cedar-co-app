import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Review {
  id: number;
  userId: number;
  userName: string;
  rating: number;
  title: string;
  body: string;
  verified: boolean;
  createdAt: string;
}

export interface CreateReviewRequest {
  rating: number;
  title: string;
  body: string;
}

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private apiUrl = environment.apiUrl + '/products';

  constructor(private http: HttpClient) {}

  getReviews(productId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiUrl}/${productId}/reviews`);
  }

  addReview(productId: number, req: CreateReviewRequest): Observable<Review> {
    return this.http.post<Review>(`${this.apiUrl}/${productId}/reviews`, req);
  }

  updateReview(productId: number, reviewId: number, req: CreateReviewRequest): Observable<Review> {
    return this.http.put<Review>(`${this.apiUrl}/${productId}/reviews/${reviewId}`, req);
  }

  deleteReview(productId: number, reviewId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${productId}/reviews/${reviewId}`);
  }
}
