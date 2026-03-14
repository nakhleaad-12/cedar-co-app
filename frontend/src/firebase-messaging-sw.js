importScripts('https://www.gstatic.com/firebasejs/9.0.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.0.0/firebase-messaging-compat.js');

// These will be configurable by the user later
const firebaseConfig = {
  apiKey: "AIzaSyBdJljrCEM1nnkMVVJh_DMB4JhnUxEcrCY",
  authDomain: "cedarco-app.firebaseapp.com",
  projectId: "cedarco-app",
  storageBucket: "cedarco-app.firebasestorage.app",
  messagingSenderId: "37913232976",
  appId: "1:37913232976:web:9bea0a940c857df4bc3355"
};

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
  console.log('[firebase-messaging-sw.js] Received background message ', payload);
  
  // If the payload has a 'notification' property, the browser shows it automatically.
  // We ONLY show it manually if it's a data-only message from FCM.
  if (payload.notification) {
    console.log('Background: Browser will handle notification display.');
    return;
  }
  const notificationOptions = {
    body: payload.notification.body || '',
    icon: '/assets/icons/icon-72x72.png',
    data: payload.data
  };

  self.registration.showNotification(notificationTitle, notificationOptions);
});
