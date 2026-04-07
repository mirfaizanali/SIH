import { Component, OnInit, signal, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule } from '@angular/material/menu';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationDto } from '../../../core/models/notification.model';
import { TimeAgoPipe } from '../../pipes/time-ago.pipe';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [MatIconModule, MatButtonModule, MatBadgeModule, MatMenuModule, TimeAgoPipe],
  templateUrl: './notification-bell.html',
  styleUrl: './notification-bell.css'
})
export class NotificationBellComponent implements OnInit {
  notificationService = inject(NotificationService);
  authService = inject(AuthService);
  notifications = signal<NotificationDto[]>([]);

  ngOnInit() {
    const token = this.authService.getAccessToken();
    if (token) {
      this.notificationService.init(token);
    }
    this.loadNotifications();
  }

  loadNotifications() {
    this.notificationService.getNotifications(0, 10).subscribe(res => {
      this.notifications.set(res.data.content);
    });
  }

  markRead(id: string) {
    this.notificationService.markAsRead(id).subscribe(() => {
      this.notifications.update(ns =>
        ns.map(n => n.id === id ? { ...n, isRead: true } : n)
      );
      this.notificationService.loadUnreadCount();
    });
  }

  markAllRead() {
    this.notificationService.markAllAsRead().subscribe(() => {
      this.notifications.update(ns => ns.map(n => ({ ...n, isRead: true })));
      this.notificationService.loadUnreadCount();
    });
  }
}
