import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService, AuthUser } from '../../services/auth.service';
import { environment } from '../../../environments/environment';
import { PushNotificationService } from '../../services/push-notification.service';
@Component({ 
  selector: 'app-account', 
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.scss']
})
export class AccountComponent implements OnInit {
  user: AuthUser | null = null;
  orders: any[] = [];
  fcmToken$: any;
  debugStatus$: any;
  constructor(private auth: AuthService, private http: HttpClient, private push: PushNotificationService) {
    this.fcmToken$ = this.push.token$;
    this.debugStatus$ = this.push.debugStatus$;
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

  sendTestPush(): void {
    // Explicitly ask for permission again (needed for iOS standalone)
    this.push.requestPermission();
    
    this.http.get(`${environment.apiUrl}/notifications/test-push`, { responseType: 'text' }).subscribe({
      next: (res) => alert('Test Push Triggered! Check your device. If nothing appears, check browser notification permission.'),
      error: (err) => console.error('Test push failed:', err)
    });
  }
}
