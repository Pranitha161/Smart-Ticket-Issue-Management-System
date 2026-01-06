import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification } from '../../shared/models/notifications.model';

@Injectable({ providedIn: 'root' })
export class Notifications {

  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.notifications}/notifications`;

  constructor(private http: HttpClient) { }

  sendNotification(notification: Notification): Observable<Notification> {
    return this.http.post<Notification>(`${this.baseUrl}/send`, notification);
  }

  getStatus(id: string): Observable<Notification> {
    return this.http.get<Notification>(`${this.baseUrl}/status/${id}`);
  }

  getHistory(userEmail: string): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.baseUrl}/history/${userEmail}`);
  }
  historySignal = signal<Notification[]>([]);

  refreshHistory(userEmail: string): void {
    this.http.get<Notification[]>(`${this.baseUrl}/history/${userEmail}`)
      .subscribe(data => {
        const sorted = data.sort((a, b) => 
          new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime()
        );
        this.historySignal.set(sorted);
      });
  }
  clearHistory(): void {
    this.historySignal.set([]);
  }
}
