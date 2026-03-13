import { Component, OnInit } from '@angular/core';
import { CartService, Cart } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss']
})
export class CartComponent implements OnInit {
  cart: Cart | null = null;

  constructor(private cartService: CartService, private auth: AuthService, private toast: ToastService) {}

  ngOnInit(): void {
    if (this.auth.isLoggedIn) {
      this.cartService.loadCart().subscribe();
    }
    this.cartService.cart$.subscribe(c => this.cart = c);
  }

  update(itemId: number, qty: number): void {
    this.cartService.updateItem(itemId, qty).subscribe();
  }

  remove(itemId: number): void {
    this.cartService.removeItem(itemId).subscribe({
      next: () => this.toast.show('Item removed from cart')
    });
  }
}
