import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Product } from './product.service';

@Injectable({ providedIn: 'root' })
export class WishlistService {
  private apiUrl = environment.apiUrl + '/wishlist';
  private wishlistSubject = new BehaviorSubject<Product[]>([]);
  wishlist$ = this.wishlistSubject.asObservable();

  get wishlistIds(): Set<number> {
    return new Set(this.wishlistSubject.value.map(p => p.id));
  }

  constructor(private http: HttpClient) {}

  load(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl).pipe(
      tap(items => this.wishlistSubject.next(items))
    );
  }

  toggle(productId: number): Observable<any> {
    const ids = this.wishlistIds;
    if (ids.has(productId)) {
      return this.http.delete<void>(`${this.apiUrl}/${productId}`).pipe(
        tap(() => {
          const updated = this.wishlistSubject.value.filter(p => p.id !== productId);
          this.wishlistSubject.next(updated);
        })
      );
    } else {
      return this.http.post<void>(`${this.apiUrl}/${productId}`, {}).pipe(
        switchMap(() => this.load())
      );
    }
  }

  isInWishlist(productId: number): boolean {
    return this.wishlistIds.has(productId);
  }
}
