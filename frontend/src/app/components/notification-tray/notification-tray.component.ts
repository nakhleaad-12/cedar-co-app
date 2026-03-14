import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppNotificationService, AppNotification } from '../../services/app-notification.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-notification-tray',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-tray.component.html',
  styleUrls: ['./notification-tray.component.scss']
})
export class NotificationTrayComponent {
  @Input() isOpen = false;
  @Output() close = new EventEmitter<void>();

  notifications$: Observable<AppNotification[]>;

  constructor(private service: AppNotificationService) {
    this.notifications$ = this.service.notifications$;
  }

  markAsRead(id: number) {
    this.service.markAsRead(id).subscribe();
  }

  markAllAsRead() {
    this.service.markAllAsRead().subscribe();
  }

  onClose() {
    this.close.emit();
  }
}
