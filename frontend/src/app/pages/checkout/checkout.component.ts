import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({ selector: 'app-checkout', templateUrl: './checkout.component.html' })
export class CheckoutComponent implements OnInit {
  street = ''; city = ''; region = ''; country = 'Lebanon';
  paymentMethod = 'CASH_ON_DELIVERY'; coupon = ''; success = false;

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private router: Router,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    // Safety net: admins should not be placing orders
    if (this.auth.isAdmin) {
      this.router.navigate(['/admin']);
    }
  }

  placeOrder(e: Event): void {
    e.preventDefault();
    this.http.post(`${environment.apiUrl}/orders`, {
      shippingStreet: this.street, shippingCity: this.city,
      shippingRegion: this.region, shippingCountry: this.country,
      paymentMethod: this.paymentMethod, couponCode: this.coupon || null
    }).subscribe({
      next: () => { this.success = true; this.toast.show('Order placed successfully!', 'success'); },
      error: () => this.toast.show('Failed to place order. Please try again.', 'error')
    });
  }
}
