import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-admin-notifications',
  templateUrl: './admin-notifications.component.html',
  styleUrls: ['./admin-notifications.component.scss']
})
export class AdminNotificationsComponent {
  title: string = '';
  body: string = '';
  isSending: boolean = false;
  message: string = '';

  constructor(private http: HttpClient) {}

  sendBroadcast() {
    if (!this.title || !this.body) {
      this.message = 'Please provide both title and body.';
      return;
    }

    this.isSending = true;
    this.message = '';

    this.http.post(`${environment.apiUrl}/notifications/broadcast`, {
      title: this.title,
      body: this.body
    }, { responseType: 'text' }).subscribe({
      next: (res) => {
        this.message = 'Broadcast sent successfully! ✅';
        this.title = '';
        this.body = '';
        this.isSending = false;
      },
      error: (err) => {
        console.error('Broadcast failed:', err);
        this.message = 'Failed to send broadcast. ❌';
        this.isSending = false;
      }
    });
  }
}
