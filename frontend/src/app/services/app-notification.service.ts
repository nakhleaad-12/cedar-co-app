import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { BehaviorSubject, Observable, interval } from 'rxjs';
import { catchError, map, startWith, switchMap, tap } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthService } from './auth.service';

export interface AppNotification {
  id: number;
  title: string;
  body: string;
  isRead: boolean;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class AppNotificationService {
  private notificationsSubject = new BehaviorSubject<AppNotification[]>([]);
  private unreadCountSubject = new BehaviorSubject<number>(0);

  notifications$ = this.notificationsSubject.asObservable();
  unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient, private auth: AuthService) {
    // Poll for changes every 30 seconds if logged in
    this.auth.currentUser$.pipe(
      map(user => !!user),
      switchMap(isLoggedIn => {
        if (isLoggedIn) {
          return interval(30000).pipe(startWith(0));
        }
        return [];
      })
    ).subscribe(() => {
      this.refresh();
    });
  }

  refresh() {
    this.fetchNotifications().subscribe();
    this.fetchUnreadCount().subscribe();
  }

  fetchNotifications(page: number = 0, size: number = 10): Observable<AppNotification[]> {
    console.log(`Fetching notifications: page=${page}, size=${size}`);
    return this.http.get<any>(`${environment.apiUrl}/notifications`, {
      params: { page: page.toString(), size: size.toString() }
    }).pipe(
      map(resp => {
        console.log('Notifications API raw response:', resp);
        const data = Array.isArray(resp) ? resp : (resp.content || []);
        console.log('Parsed notifications data:', data);
        return data;
      }),
      tap(notifs => this.notificationsSubject.next(notifs)),
      catchError(err => {
        console.error('Error fetching notifications:', err);
        return of([]);
      })
    );
  }

  fetchUnreadCount(): Observable<number> {
    return this.http.get<number>(`${environment.apiUrl}/notifications/unread-count`).pipe(
      tap(count => {
        console.log('Unread count response:', count);
        this.unreadCountSubject.next(count);
      }),
      catchError(err => {
        console.error('Error fetching unread count:', err);
        return of(0);
      })
    );
  }

  markAsRead(id: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/notifications/${id}/read`, {}).pipe(
      tap(() => {
        const current = this.notificationsSubject.value.map(n => 
          n.id === id ? { ...n, isRead: true } : n
        );
        this.notificationsSubject.next(current);
        this.fetchUnreadCount().subscribe();
      })
    );
  }

  markAllAsRead(): Observable<any> {
    return this.http.post(`${environment.apiUrl}/notifications/mark-all-read`, {}).pipe(
      tap(() => {
        const current = this.notificationsSubject.value.map(n => ({ ...n, isRead: true }));
        this.notificationsSubject.next(current);
        this.unreadCountSubject.next(0);
      })
    );
  }
}
