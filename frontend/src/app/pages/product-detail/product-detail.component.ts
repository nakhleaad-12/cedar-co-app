import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProductService, Product } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { ToastService } from '../../services/toast.service';
import { AuthService } from '../../services/auth.service';
import { WishlistService } from '../../services/wishlist.service';
import { ReviewService, Review } from '../../services/review.service';

@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss']
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;
  selectedImage = '';
  selectedSize = '';
  selectedColor = '';
  qty = 1;
  addedMsg = '';
  
  reviews: Review[] = [];
  reviewForm = {
    rating: 5,
    title: '',
    body: ''
  };
  submittingReview = false;
  editingReview: Review | null = null;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private cartService: CartService,
    private wishlist: WishlistService,
    private reviewService: ReviewService,
    private toast: ToastService,
    public auth: AuthService
  ) {}

  get inWishlist(): boolean {
    return !!this.product && this.wishlist.isInWishlist(this.product.id);
  }

  ngOnInit(): void {
    this.route.params.subscribe(p => {
      this.productService.getBySlug(p['slug']).subscribe(prod => {
        this.product = prod;
        this.selectedImage = prod.images[0] || '';
        this.selectedSize = prod.sizes[0] || '';
        this.selectedColor = prod.colors[0] || '';
        this.loadReviews(prod.id);
      });
    });
  }

  loadReviews(productId: number): void {
    this.reviewService.getReviews(productId).subscribe(revs => {
      this.reviews = revs;
    });
  }

  submitReview(): void {
    if (!this.auth.isLoggedIn) {
      this.toast.show('Please sign in to leave a review', 'info');
      return;
    }
    if (!this.product) return;
    if (!this.reviewForm.title || !this.reviewForm.body) {
      this.toast.show('Please fill in both title and comment', 'info');
      return;
    }

    this.submittingReview = true;
    this.reviewService.addReview(this.product.id, this.reviewForm).subscribe({
      next: (res) => {
        this.reviews.unshift(res);
        this.reviewForm = { rating: 5, title: '', body: '' };
        this.submittingReview = false;
        this.toast.show('Review submitted!', 'success');
        // Refresh product info to update ratings and counts
        this.productService.getBySlug(this.product!.slug).subscribe(p => this.product = p);
      },
      error: () => {
        this.submittingReview = false;
        this.toast.show('Failed to submit review', 'error');
      }
    });
  }

  startEdit(rev: Review): void {
    this.editingReview = { ...rev };
    this.reviewForm = {
      rating: rev.rating,
      title: rev.title,
      body: rev.body
    };
    // Scroll to form?
    document.querySelector('.review-form-box')?.scrollIntoView({ behavior: 'smooth' });
  }

  cancelEdit(): void {
    this.editingReview = null;
    this.reviewForm = { rating: 5, title: '', body: '' };
  }

  deleteReview(reviewId: number): void {
    if (!confirm('Are you sure you want to delete this review?')) return;
    if (!this.product) return;

    this.reviewService.deleteReview(this.product.id, reviewId).subscribe({
      next: () => {
        this.reviews = this.reviews.filter(r => r.id !== reviewId);
        this.toast.show('Review deleted', 'success');
        this.productService.getBySlug(this.product!.slug).subscribe(p => this.product = p);
      },
      error: () => this.toast.show('Failed to delete review', 'error')
    });
  }

  updateReview(): void {
    if (!this.product || !this.editingReview) return;
    if (!this.reviewForm.title || !this.reviewForm.body) {
      this.toast.show('Please fill in both title and comment', 'info');
      return;
    }

    this.submittingReview = true;
    this.reviewService.updateReview(this.product.id, this.editingReview.id, this.reviewForm).subscribe({
      next: (res) => {
        const index = this.reviews.findIndex(r => r.id === res.id);
        if (index !== -1) this.reviews[index] = res;
        this.cancelEdit();
        this.submittingReview = false;
        this.toast.show('Review updated!', 'success');
        this.productService.getBySlug(this.product!.slug).subscribe(p => this.product = p);
      },
      error: () => {
        this.submittingReview = false;
        this.toast.show('Failed to update review', 'error');
      }
    });
  }

  addToCart(): void {
    if (!this.auth.isLoggedIn) { this.toast.show('Please sign in to add to cart', 'info'); return; }
    if (!this.product) return;
    this.cartService.addItem(this.product.id, this.selectedSize, this.selectedColor, this.qty).subscribe({
      next: () => {
        this.addedMsg = '✓ Added!';
        this.toast.show(`${this.product!.name} added to cart`, 'success');
        setTimeout(() => this.addedMsg = '', 2500);
      },
      error: () => this.toast.show('Failed to add item. Please try again.', 'error')
    });
  }

  toggleWishlist(): void {
    if (!this.auth.isLoggedIn) {
      this.toast.show('Sign in to save to wishlist', 'info');
      return;
    }
    if (!this.product) return;
    this.wishlist.toggle(this.product.id).subscribe({
      next: () => this.toast.show(
        this.inWishlist ? 'Added to wishlist' : 'Removed from wishlist', 'success'
      )
    });
  }
}
