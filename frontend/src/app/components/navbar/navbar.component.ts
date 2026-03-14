import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';
import { WishlistService } from '../../services/wishlist.service';
import { PushNotificationService } from '../../services/push-notification.service';
import { AppNotificationService } from '../../services/app-notification.service';
import { NotificationTrayComponent } from '../notification-tray/notification-tray.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, NotificationTrayComponent],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  scrolled = false;
  menuOpen = false;
  userMenuOpen = false;
  cartCount = 0;
  wishlistCount = 0;
  isHome = false;
  unreadNotificationsCount = 0;
  notificationTrayOpen = false;

  constructor(
    public auth: AuthService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    public push: PushNotificationService,
    public notificationService: AppNotificationService,
    private router: Router
  ) {
    this.router.events.subscribe(() => {
      this.isHome = this.router.url === '/' || this.router.url === '/home';
    });
  }

  get isLoggedIn(): boolean { return this.auth.isLoggedIn; }
  get isAdmin(): boolean { return this.auth.isAdmin; }

  ngOnInit(): void {
    this.cartService.cart$.subscribe(cart => {
      this.cartCount = cart?.items.reduce((s,i) => s + i.quantity, 0) ?? 0;
    });
    this.wishlistService.wishlist$.subscribe(items => {
      this.wishlistCount = items.length;
    });
    if (this.auth.isLoggedIn) {
      this.cartService.loadCart().subscribe();
      this.wishlistService.load().subscribe();
    }
    this.notificationService.unreadCount$.subscribe(count => {
      this.unreadNotificationsCount = count;
    });
  }

  @HostListener('window:scroll')
  onScroll(): void {
    this.scrolled = window.scrollY > 40;
  }

  logout(): void {
    this.auth.logout();
    this.userMenuOpen = false;
    this.router.navigate(['/']);
  }

  enableNotifications(): void {
    this.push.requestPermission();
    this.userMenuOpen = false;
    this.menuOpen = false;
  }

  toggleNotifications(): void {
    if (this.push.tokenSubject.value) {
      this.push.unsubscribe();
    } else {
      this.push.requestPermission();
    }
  }
}
