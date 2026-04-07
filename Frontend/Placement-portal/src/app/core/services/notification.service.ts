import { Injectable, signal } from '@angular/core';
import { tap } from 'rxjs';
import { ApiService } from './api.service';
import { WebSocketService } from './websocket.service';
import { ApiResponse, PagedResponse } from '../models/api.model';
import { NotificationDto } from '../models/notification.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private _unreadCount = signal(0);
  readonly unreadCount = this._unreadCount.asReadonly();

  constructor(private api: ApiService, private ws: WebSocketService) {}

  init(accessToken: string): void {
    this.ws.connect(accessToken);
    this.ws.notifications$.subscribe(() => {
      this._unreadCount.update(c => c + 1);
    });
    this.loadUnreadCount();
  }

  loadUnreadCount(): void {
    this.api.get<{ count: number }>('/api/notifications/unread-count')
      .subscribe(res => this._unreadCount.set(res.data.count));
  }

  getNotifications(page = 0, size = 20): Observable<ApiResponse<PagedResponse<NotificationDto>>> {
    return this.api.get<PagedResponse<NotificationDto>>('/api/notifications', { page, size });
  }

  markAsRead(id: string): Observable<ApiResponse<NotificationDto>> {
    return this.api.patch<NotificationDto>(`/api/notifications/${id}/read`);
  }

  markAllAsRead(): Observable<ApiResponse<{ updated: number }>> {
    return this.api.patch<{ updated: number }>('/api/notifications/read-all')
      .pipe(tap(() => this._unreadCount.set(0)));
  }
}
