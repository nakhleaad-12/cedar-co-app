import { Component, OnInit } from '@angular/core';
import { WishlistService } from '../../services/wishlist.service';
import { Product } from '../../services/product.service';
@Component({ selector: 'app-wishlist', templateUrl: './wishlist.component.html' })
export class WishlistComponent implements OnInit {
  products: Product[] = [];
  constructor(private wishlist: WishlistService) {}
  ngOnInit(): void { this.wishlist.load().subscribe(p => this.products = p); }
}
