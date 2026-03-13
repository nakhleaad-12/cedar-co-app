import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AuthUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  accessToken: string;
  refreshToken: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = environment.apiUrl + '/auth';
  private currentUserSubject = new BehaviorSubject<AuthUser | null>(this.loadUser());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  get currentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  get isLoggedIn(): boolean {
    return !!this.currentUser;
  }

  get isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  register(payload: { firstName: string; lastName: string; email: string; password: string; phone?: string }): Observable<AuthUser> {
    return this.http.post<AuthUser>(`${this.apiUrl}/register`, payload).pipe(
      tap(user => this.saveUser(user))
    );
  }

  login(email: string, password: string): Observable<AuthUser> {
    return this.http.post<AuthUser>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap(user => this.saveUser(user))
    );
  }

  logout(): void {
    localStorage.removeItem('cedar_user');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return this.currentUser?.accessToken ?? null;
  }

  private saveUser(user: AuthUser): void {
    localStorage.setItem('cedar_user', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  private loadUser(): AuthUser | null {
    try {
      const data = localStorage.getItem('cedar_user');
      return data ? JSON.parse(data) : null;
    } catch {
      return null;
    }
  }
}
