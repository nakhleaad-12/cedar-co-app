import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ToastService } from '../../../services/toast.service';

interface Product {
  id: number;
  name: string;
  slug: string;
  sku: string;
  price: number;
  salePrice?: number;
  category?: string;
  collection?: string;
  gender: string;
  active: boolean;
  images: string[];
  sizes: string[];
  colors: string[];
  stockMap: Record<string, number>;
  featured: boolean;
  newArrival: boolean;
  bestSeller: boolean;
  rating: number;
  reviewCount: number;
}

@Component({
  selector: 'app-admin-products',
  templateUrl: './admin-products.component.html',
  styles: [`
    .admin-page { padding-top: 100px; padding-bottom: 5rem; min-height: 80vh; }
    .admin-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem; }
    .table-wrapper { overflow-x: auto; background: #fff; border-radius: var(--radius); box-shadow: 0 1px 3px rgba(0,0,0,0.08); -webkit-overflow-scrolling: touch; }
    .products-table { width: 100%; border-collapse: collapse; min-width: 900px; }
    .products-table th, .products-table td { padding: 0.85rem 1rem; text-align: left; border-bottom: 1px solid var(--border); font-size: 0.9rem; }
    .products-table th { background: var(--midnight); color: #fff; font-size: 0.78rem; text-transform: uppercase; letter-spacing: .6px; }
    .products-table tr:last-child td { border-bottom: none; }
    .thumb { width: 48px; height: 48px; object-fit: cover; border-radius: 6px; }
    .badge { display: inline-block; padding: 2px 8px; border-radius: 20px; font-size: 0.72rem; font-weight: 600; }
    .badge-active { background: #d4edda; color: #155724; }
    .badge-inactive { background: #f8d7da; color: #721c24; }
    .create-panel { background: #fff; border: 1px solid var(--border); border-radius: var(--radius); padding: 1.5rem; margin-bottom: 2rem; }
    .create-panel h2 { margin-bottom: 1.2rem; font-size: 1.1rem; font-family: var(--font-head); }
    .form-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.35rem; }
    .form-group label { font-size: 0.8rem; font-weight: 600; color: var(--muted); text-transform: uppercase; letter-spacing: .5px; }
    .form-group input, .form-group select, .form-group textarea { padding: 0.65rem 0.85rem; border: 1.5px solid var(--border); border-radius: 10px; font-size: 0.9rem; transition: var(--transition); }
    .form-group input:focus { border-color: var(--lebanese-red); outline: none; }
    .form-actions { display: flex; gap: 1rem; margin-top: 2rem; }
    @media (max-width: 600px) {
      .form-grid { grid-template-columns: 1fr; }
      .form-group { grid-column: span 1 !important; }
      .form-actions { flex-direction: column; button { width: 100%; } }
      .create-panel { padding: 1rem; }
    }
  `]
})
export class AdminProductsComponent implements OnInit {
  products: Product[] = [];
  loading = true;
  showCreateForm = false;
  creating = false;

  // Create form fields
  form = {
    name: '', slug: '', sku: '', description: '',
    price: '', salePrice: '',
    gender: 'UNISEX',
    sizes: '', colors: '',
    imageUrl: '',
    featured: false, newArrival: false, bestSeller: false
  };

  constructor(private http: HttpClient, private toast: ToastService) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.http.get<any>(`${environment.apiUrl}/products?size=100&page=0`).subscribe({
      next: res => {
        this.products = res.content ?? res;
        this.loading = false;
      },
      error: () => {
        this.toast.show('Failed to load products', 'error');
        this.loading = false;
      }
    });
  }

  autoSlug(): void {
    if (!this.form.slug) {
      this.form.slug = this.form.name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '');
    }
  }

  createProduct(): void {
    if (!this.form.name || !this.form.price || !this.form.sku) {
      this.toast.show('Name, SKU and Price are required', 'error');
      return;
    }
    this.creating = true;
    const sizes = this.form.sizes.split(',').map(s => s.trim()).filter(Boolean);
    const colors = this.form.colors.split(',').map(c => c.trim()).filter(Boolean);
    const stockMap: Record<string, number> = {};
    sizes.forEach(s => { stockMap[s] = 10; });

    const payload: any = {
      name: this.form.name,
      slug: this.form.slug || this.form.name.toLowerCase().replace(/[^a-z0-9]+/g, '-'),
      sku: this.form.sku,
      description: this.form.description,
      price: parseFloat(this.form.price),
      salePrice: this.form.salePrice ? parseFloat(this.form.salePrice) : null,
      gender: this.form.gender,
      sizes, colors,
      stockMap,
      images: this.form.imageUrl ? [this.form.imageUrl] : [],
      featured: this.form.featured,
      newArrival: this.form.newArrival,
      bestSeller: this.form.bestSeller
    };

    this.http.post<Product>(`${environment.apiUrl}/admin/products`, payload).subscribe({
      next: p => {
        this.products = [p, ...this.products];
        this.toast.show(`Product "${p.name}" created!`, 'success');
        this.showCreateForm = false;
        this.resetForm();
        this.creating = false;
      },
      error: () => {
        this.toast.show('Failed to create product', 'error');
        this.creating = false;
      }
    });
  }

  deleteProduct(p: Product): void {
    if (!confirm(`Delete "${p.name}"? This will soft-delete it from the store.`)) return;
    this.http.delete(`${environment.apiUrl}/admin/products/${p.id}`).subscribe({
      next: () => {
        this.products = this.products.filter(x => x.id !== p.id);
        this.toast.show(`"${p.name}" deleted`, 'success');
      },
      error: () => this.toast.show('Failed to delete product', 'error')
    });
  }

  resetForm(): void {
    this.form = {
      name: '', slug: '', sku: '', description: '',
      price: '', salePrice: '', gender: 'UNISEX',
      sizes: '', colors: '', imageUrl: '',
      featured: false, newArrival: false, bestSeller: false
    };
  }

  totalStock(p: Product): number {
    return Object.values(p.stockMap ?? {}).reduce((a, b) => a + b, 0);
  }
}
