import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class Activity {

  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.tickets}`;

  constructor(private http: HttpClient) { }

  getTicketActivity(ticketId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/tickets/${ticketId}/activity`);
  }

  logActivity(ticketId: string, actorId: string, comment: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/tickets/${ticketId}/activity/comment`, null, { params: { actorId, comment } });
  }
}
