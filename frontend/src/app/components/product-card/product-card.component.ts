import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { Product } from '../../services/product.service';
import { WishlistService } from '../../services/wishlist.service';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-product-card',
  templateUrl: './product-card.component.html',
  styleUrls: ['./product-card.component.scss']
})
export class ProductCardComponent {
  @Input() product!: Product;

  constructor(
    private router: Router,
    private wishlist: WishlistService,
    private auth: AuthService,
    private toast: ToastService
  ) {}

  get inWishlist(): boolean {
    return this.wishlist.isInWishlist(this.product.id);
  }

  navigate(): void {
    this.router.navigate(['/shop', this.product.slug]);
  }

  toggleWishlist(e: Event): void {
    e.stopPropagation();
    if (!this.auth.isLoggedIn) {
      this.toast.show('Sign in to save to wishlist', 'info');
      return;
    }
    this.wishlist.toggle(this.product.id).subscribe({
      next: () => this.toast.show(
        this.inWishlist ? 'Added to wishlist' : 'Removed from wishlist', 'success'
      )
    });
  }
}
