import { Component, HostListener, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';
import { Router } from '@angular/router';
import { WishlistService } from '../../services/wishlist.service';
import { PushNotificationService } from '../../services/push-notification.service';

@Component({
  selector: 'app-navbar',
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

  constructor(
    public auth: AuthService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    public push: PushNotificationService,
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
    // We call requestPermission which handles the prompt if needed
    this.push.requestPermission();
  }
}
