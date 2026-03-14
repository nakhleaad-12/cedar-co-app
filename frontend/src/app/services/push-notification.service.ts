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
  private debugStatusSubject = new BehaviorSubject<string>('Initializing...');
  token$ = this.tokenSubject.asObservable();
  debugStatus$ = this.debugStatusSubject.asObservable();

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
    if (!this.messaging) {
      this.debugStatusSubject.next('Error: Firebase Messaging not initialized');
      return;
    }

    this.debugStatusSubject.next('Requesting permission...');
    try {
      if (!('Notification' in window)) {
        this.debugStatusSubject.next('Error: Browser does not support Notifications');
        return;
      }
      const permission = await Notification.requestPermission();
      this.debugStatusSubject.next(`Permission: ${permission}`);
      if (permission === 'granted') {
        await this.registerAndGetToken();
      }
    } catch (error: any) {
      this.debugStatusSubject.next(`Error: ${error.message || error}`);
    }
  }

  private async registerAndGetToken() {
    try {
      this.debugStatusSubject.next('Registering Service Worker...');
      const registration = await navigator.serviceWorker.register('/firebase-messaging-sw.js');
      
      this.debugStatusSubject.next('Waiting for SW activation...');
      if (!registration.active) {
        await new Promise<void>((resolve, reject) => {
          const timeout = setTimeout(() => reject(new Error('SW activation timeout')), 10000);
          registration.addEventListener('activate', () => {
            clearTimeout(timeout);
            resolve();
          }, { once: true });
          if (!registration.installing && !registration.waiting) resolve();
        });
      }

      this.debugStatusSubject.next('Getting Firebase Token...');
      const currentToken = await getToken(this.messaging, {
        vapidKey: this.firebaseConfig.vapidKey,
        serviceWorkerRegistration: registration
      });

      if (currentToken) {
        this.debugStatusSubject.next('Connected ✅');
        this.tokenSubject.next(currentToken);
        this.syncTokenWithBackend(currentToken);
      } else {
        this.debugStatusSubject.next('Error: No token received');
      }
    } catch (err: any) {
      this.debugStatusSubject.next(`Error: ${err.message || err}`);
      console.error('FCM Token error:', err);
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
