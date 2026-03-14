import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService, AuthUser } from '../../services/auth.service';
import { environment } from '../../../environments/environment';
@Component({ 
  selector: 'app-account', 
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.scss']
})
export class AccountComponent implements OnInit {
  user: AuthUser | null = null;
  orders: any[] = [];
  constructor(private auth: AuthService, private http: HttpClient) {
  }
  ngOnInit(): void {
    this.auth.currentUser$.subscribe(u => {
      this.user = u;
      if (u) {
        console.log('Fetching orders for user:', u.email);
        this.http.get<any[]>(`${environment.apiUrl}/orders`).subscribe({
          next: o => {
            console.log('Orders received:', o.length);
            this.orders = o;
          },
          error: (err) => {
            console.error('Failed to fetch orders:', err);
          }
        });
      } else {
        console.log('No user loaded in AccountComponent');
        this.orders = [];
      }
    });
  }
}
