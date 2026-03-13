import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}
  canActivate(): boolean {
    if (this.auth.isLoggedIn) return true;
    this.router.navigate(['/auth/login']);
    return false;
  }
}

@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}
  canActivate(): boolean {
    if (this.auth.isAdmin) return true;
    this.router.navigate(['/']);
    return false;
  }
}

/** Allows only logged-in, non-admin (customer) users.
 *  Admins are redirected to /admin; guests go to /auth/login. */
@Injectable({ providedIn: 'root' })
export class CustomerGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}
  canActivate(): boolean {
    if (!this.auth.isLoggedIn) {
      this.router.navigate(['/auth/login']);
      return false;
    }
    if (this.auth.isAdmin) {
      this.router.navigate(['/admin']);
      return false;
    }
    return true;
  }
}
