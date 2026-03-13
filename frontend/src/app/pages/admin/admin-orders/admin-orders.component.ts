import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ToastService } from '../../../services/toast.service';

interface OrderItem {
  productName: string;
  size: string;
  color: string;
  quantity: number;
  unitPrice: number;
}

interface Order {
  id: number;
  userEmail: string;
  createdAt: string;
  subtotal: number;
  discount: number;
  total: number;
  status: string;
  paymentStatus: string;
  paymentMethod: string;
  shippingCity: string;
  shippingCountry: string;
  items: OrderItem[];
  updatingStatus?: boolean;
}

const STATUS_OPTIONS = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
const PAYMENT_OPTIONS = ['PENDING', 'PAID', 'FAILED', 'REFUNDED'];

@Component({
  selector: 'app-admin-orders',
  templateUrl: './admin-orders.component.html',
  styles: [`
    .admin-page { padding-top: 100px; padding-bottom: 5rem; min-height: 80vh; }
    .admin-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem; }
    .orders-table { width: 100%; border-collapse: collapse; background: #fff; border-radius: var(--radius); overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
    .orders-table th, .orders-table td { padding: 0.85rem 1rem; text-align: left; border-bottom: 1px solid var(--border); font-size: 0.88rem; }
    .orders-table th { background: var(--midnight); color: #fff; font-size: 0.76rem; text-transform: uppercase; letter-spacing: .6px; }
    .orders-table tr:last-child td { border-bottom: none; }
    .orders-table tr:hover td { background: #fafaf8; }
    .badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 0.72rem; font-weight: 700; text-transform: uppercase; letter-spacing: .5px; }
    .status-PENDING { background: #fff3cd; color: #856404; }
    .status-PROCESSING { background: #cce5ff; color: #004085; }
    .status-SHIPPED { background: #d4edda; color: #155724; }
    .status-DELIVERED { background: #c3e6cb; color: #0f5132; }
    .status-CANCELLED { background: #f8d7da; color: #721c24; }
    .status-select { padding: 0.3rem 0.6rem; border: 1px solid var(--border); border-radius: var(--radius); font-size: 0.82rem; cursor: pointer; }
    .pagination { display: flex; align-items: center; gap: 1rem; justify-content: center; margin-top: 1.5rem; }
    .pagination button { padding: 0.4rem 1rem; border: 1px solid var(--border); background: #fff; border-radius: var(--radius); cursor: pointer; }
    .pagination button:disabled { opacity: 0.4; cursor: default; }
    .page-info { font-size: 0.88rem; color: var(--muted); }
    .loading { text-align: center; padding: 3rem; color: var(--muted); }
    .empty { text-align: center; padding: 3rem; color: var(--muted); font-size: 1rem; }
    .items-tooltip { font-size: 0.78rem; color: var(--muted); margin-top: 3px; }
    .muted { color: var(--muted); }
  `]
})
export class AdminOrdersComponent implements OnInit {
  orders: Order[] = [];
  loading = true;
  page = 0;
  pageSize = 15;
  totalPages = 1;
  totalElements = 0;
  statusOptions = STATUS_OPTIONS;
  paymentOptions = PAYMENT_OPTIONS;

  constructor(private http: HttpClient, private toast: ToastService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.http.get<any>(`${environment.apiUrl}/admin/orders?page=${this.page}&size=${this.pageSize}`).subscribe({
      next: res => {
        this.orders = (res.content ?? []).map((o: any) => ({ ...o, updatingStatus: false }));
        this.totalPages = res.totalPages ?? 1;
        this.totalElements = res.totalElements ?? this.orders.length;
        this.loading = false;
      },
      error: () => {
        this.toast.show('Failed to load orders', 'error');
        this.loading = false;
      }
    });
  }

  updateStatus(order: Order, newStatus: string): void {
    if (order.status === newStatus) return;
    order.updatingStatus = true;
    this.http.put<Order>(`${environment.apiUrl}/admin/orders/${order.id}/status`, { status: newStatus }).subscribe({
      next: updated => {
        order.status = updated.status;
        order.updatingStatus = false;
        this.toast.show(`Order #${order.id} → ${updated.status}`, 'success');
      },
      error: () => {
        order.updatingStatus = false;
        this.toast.show('Failed to update status', 'error');
      }
    });
  }

  updatePayment(order: Order, newStatus: string): void {
    if (order.paymentStatus === newStatus) return;
    order.updatingStatus = true;
    this.http.put<Order>(`${environment.apiUrl}/admin/orders/${order.id}/payment-status`, { paymentStatus: newStatus }).subscribe({
      next: updated => {
        order.paymentStatus = updated.paymentStatus;
        order.updatingStatus = false;
        this.toast.show(`Payment for #${order.id} → ${updated.paymentStatus}`, 'success');
      },
      error: () => {
        order.updatingStatus = false;
        this.toast.show('Failed to update payment status', 'error');
      }
    });
  }

  prevPage(): void { if (this.page > 0) { this.page--; this.loadOrders(); } }
  nextPage(): void { if (this.page < this.totalPages - 1) { this.page++; this.loadOrders(); } }

  formatDate(iso: string): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  formatCurrency(v: number): string {
    return '$' + (v ?? 0).toFixed(2);
  }

  itemsSummary(order: Order): string {
    if (!order.items?.length) return '—';
    return order.items.map(i => `${i.quantity}× ${i.productName}`).join(', ');
  }
}
