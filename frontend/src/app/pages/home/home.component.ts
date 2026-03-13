import { Component, OnInit } from '@angular/core';
import { ProductService, Product } from '../../services/product.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  newArrivals: Product[] = [];
  bestSellers: Product[] = [];
  collections: any[] = [];
  newsletterEmail = '';

  constructor(
    private productService: ProductService,
    private http: HttpClient,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.productService.getNewArrivals().subscribe(p => this.newArrivals = p.slice(0, 4));
    this.productService.getBestSellers().subscribe(p => this.bestSellers = p.slice(0, 4));
    this.http.get<any[]>(`${environment.apiUrl}/collections`).subscribe(c => this.collections = c);
  }

  onSubscribe(e: Event): void {
    e.preventDefault();
    if (this.newsletterEmail) {
      this.toast.show('🌿 Thanks for subscribing! Use code WELCOME15 for 15% off.', 'success');
      this.newsletterEmail = '';
    }
  }
}
