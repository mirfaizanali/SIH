import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { Client } from '@stomp/stompjs';
import { environment } from '../../../environments/environment';
import { NotificationDto } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private client: Client | null = null;
  private notificationSubject = new Subject<NotificationDto>();

  readonly notifications$ = this.notificationSubject.asObservable();

  connect(accessToken: string): void {
    if (this.client?.active) return;
    this.client = new Client({
      brokerURL: environment.wsUrl,
      connectHeaders: { Authorization: `Bearer ${accessToken}` },
      reconnectDelay: 5000,
      onConnect: () => {
        this.client!.subscribe('/user/queue/notifications', (message) => {
          try {
            const notification: NotificationDto = JSON.parse(message.body);
            this.notificationSubject.next(notification);
          } catch {}
        });
      },
      onStompError: (frame) => console.error('STOMP error', frame)
    });
    this.client.activate();
  }

  disconnect(): void {
    this.client?.deactivate();
    this.client = null;
  }
}
