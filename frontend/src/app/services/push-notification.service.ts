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

  async requestPermission() {
    if (!this.messaging) return;

    try {
      const permission = await Notification.requestPermission();
      if (permission === 'granted') {
        console.log('Notification permission granted.');
        await this.registerAndGetToken();
      } else {
        console.warn('Notification permission denied:', permission);
      }
    } catch (error) {
      console.error('Error requesting notification permission:', error);
    }
  }

  private async registerAndGetToken() {
    try {
      // Explicitly register service worker and wait for it to be active
      const registration = await navigator.serviceWorker.register('/firebase-messaging-sw.js');
      
      // Wait for service worker to be active
      if (!registration.active) {
        await new Promise<void>((resolve) => {
          registration.addEventListener('activate', () => resolve(), { once: true });
          // If already redundant or failed, we might wait forever, so let's check state
          if (registration.installing || registration.waiting) {
            // waiting...
          } else {
            resolve();
          }
        });
      }

      const currentToken = await getToken(this.messaging, {
        vapidKey: this.firebaseConfig.vapidKey,
        serviceWorkerRegistration: registration
      });

      if (currentToken) {
        console.log('FCM Token:', currentToken);
        this.tokenSubject.next(currentToken);
        this.syncTokenWithBackend(currentToken);
      } else {
        console.log('No registration token available. Request permission to generate one.');
      }
    } catch (err) {
      console.log('An error occurred while retrieving token. ', err);
    }
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
      if (payload.notification) {
        // Use service worker to show notification for better mobile support
        navigator.serviceWorker.ready.then(registration => {
          registration.showNotification(payload.notification.title || 'New Notification', {
            body: payload.notification.body,
            icon: '/assets/icons/icon-72x72.png',
            badge: '/assets/icons/icon-72x72.png', // Small icon for notification bar
            vibrate: [100, 50, 100],
            data: payload.data,
            tag: 'order-notification' // Prevents duplicate notifications
          } as any);
        });
      }
    });
  }
}
