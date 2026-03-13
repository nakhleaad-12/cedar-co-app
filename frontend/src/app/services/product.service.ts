import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Product {
  id: number;
  name: string;
  slug: string;
  description: string;
  price: number;
  salePrice: number | null;
  categoryName: string;
  collectionName: string;
  collectionSlug: string;
  gender: string;
  images: string[];
  sizes: string[];
  colors: string[];
  stockMap: Record<string, number>;
  featured: boolean;
  newArrival: boolean;
  bestSeller: boolean;
  rating: number;
  reviewCount: number;
  createdAt: string;
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class ProductService {
  private apiUrl = environment.apiUrl + '/products';

  constructor(private http: HttpClient) {}

  getAll(page = 0, size = 12, sort?: string): Observable<PageResult<Product>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (sort) params = params.set('sort', sort);
    return this.http.get<PageResult<Product>>(this.apiUrl, { params });
  }

  getBySlug(slug: string): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${slug}`);
  }

  getFeatured(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/featured`);
  }

  getNewArrivals(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/new-arrivals`);
  }

  getBestSellers(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/best-sellers`);
  }

  getProductReviews(productId: number): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/products/${productId}/reviews`);
  }

  addReview(productId: number, payload: { rating: number; title: string; body: string }): Observable<any> {
    return this.http.post(`${environment.apiUrl}/products/${productId}/reviews`, payload);
  }
}
