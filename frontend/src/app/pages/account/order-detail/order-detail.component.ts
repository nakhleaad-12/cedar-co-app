import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-order-detail',
  templateUrl: './order-detail.component.html',
  styleUrls: ['./order-detail.component.scss']
})
export class OrderDetailComponent implements OnInit {
  order: any = null;
  loading = true;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.fetchOrder(id);
    } else {
      this.error = 'Invalid order ID.';
      this.loading = false;
    }
  }

  fetchOrder(id: string): void {
    this.http.get<any>(`${environment.apiUrl}/orders/${id}`).subscribe({
      next: (data) => {
        this.order = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load order detailed:', err);
        this.error = 'Unable to load order details. Please ensure the order exists.';
        this.loading = false;
      }
    });
  }
}
