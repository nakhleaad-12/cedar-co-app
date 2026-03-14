import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';
import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PushNotificationService {
  private messaging: any;
  private tokenSubject = new BehaviorSubject<string | null>(null);
  token$ = this.tokenSubject.asObservable();

  private firebaseConfig = environment.firebase;

  constructor(private http: HttpClient, private auth: AuthService) {
    try {
      const app = initializeApp(this.firebaseConfig);
      this.messaging = getMessaging(app);
    } catch (e) {
      console.warn('Firebase initialization failed. Push notifications may not work.', e);
    }
  }

  requestPermission() {
    if (!this.messaging) return;

    Notification.requestPermission().then((permission) => {
      if (permission === 'granted') {
        console.log('Notification permission granted.');
        this.getAndSaveToken();
      } else {
        console.warn('Unable to get permission to notify.');
      }
    });
  }

  private getAndSaveToken() {
    getToken(this.messaging, { vapidKey: this.firebaseConfig.vapidKey })
      .then((currentToken) => {
        if (currentToken) {
          console.log('FCM Token:', currentToken);
          this.tokenSubject.next(currentToken);
          this.syncTokenWithBackend(currentToken);
        } else {
          console.log('No registration token available. Request permission to generate one.');
        }
      })
      .catch((err) => {
        console.log('An error occurred while retrieving token. ', err);
      });
  }

  private syncTokenWithBackend(token: string) {
    this.auth.currentUser$.subscribe(user => {
      if (user) {
        this.http.post(`${environment.apiUrl}/notifications/tokens`, { token }).subscribe({
          next: () => console.log('Token synced with backend'),
          error: (err) => console.error('Failed to sync token', err)
        });
      }
    });
  }

  listenForMessages() {
    if (!this.messaging) return;
    onMessage(this.messaging, (payload) => {
      console.log('Message received. ', payload);
      // You can trigger a toast or update local state here
    });
  }
}
