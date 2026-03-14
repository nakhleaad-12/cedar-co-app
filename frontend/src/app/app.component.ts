import { Component, OnInit } from '@angular/core';
import { PushNotificationService } from './services/push-notification.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
})
export class AppComponent implements OnInit {
  constructor(private push: PushNotificationService) {}

  ngOnInit(): void {
    this.push.requestPermission();
    this.push.listenForMessages();
  }
}
