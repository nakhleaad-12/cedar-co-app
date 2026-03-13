import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styles: [`.stat-card { display:flex; flex-direction:column; gap:0.5rem; padding:1.5rem; background:#fff; border:1px solid var(--border); border-radius:var(--radius); } .stat-label { font-size:0.8rem; text-transform:uppercase; letter-spacing:1px; color:var(--muted); } .stat-value { font-size:2rem; font-weight:700; font-family:var(--font-head); color:var(--midnight); } .loading-row { display:flex; justify-content:center; padding:4rem; }`]
})
export class AdminDashboardComponent implements OnInit {
  stats: any = null;
  statCards: any[] = [];
  constructor(private http: HttpClient) {}
  ngOnInit(): void {
    this.http.get<any>(`${environment.apiUrl}/admin/dashboard/stats`).subscribe({
      next: s => {
        this.stats = s;
        this.statCards = [
          { label: 'Total Products', value: s.totalProducts },
          { label: 'Total Orders', value: s.totalOrders },
          { label: 'Customers', value: s.totalCustomers },
          { label: 'Revenue', value: '$' + (s.totalRevenue || 0).toFixed(2) }
        ];
      }
    });
  }
}
