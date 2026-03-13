import { Component, OnInit } from '@angular/core';
import { ProductService, Product } from '../../services/product.service';

@Component({
  selector: 'app-shop',
  templateUrl: './shop.component.html',
  styleUrls: ['./shop.component.scss']
})
export class ShopComponent implements OnInit {
  products: Product[] = [];
  totalProducts = 0;
  totalPages = 0;
  currentPage = 0;
  sortBy = 'newest';
  loading = false;

  constructor(private productService: ProductService) {}

  ngOnInit(): void { this.loadProducts(); }

  loadProducts(): void {
    this.loading = true;
    this.productService.getAll(this.currentPage, 12, this.sortBy).subscribe(res => {
      this.products = res.content;
      this.totalProducts = res.totalElements;
      this.totalPages = res.totalPages;
      this.loading = false;
    });
  }

  onSort(): void { this.currentPage = 0; this.loadProducts(); }
  goTo(page: number): void { this.currentPage = page; this.loadProducts(); window.scrollTo({ top: 0, behavior: 'smooth' }); }
}
