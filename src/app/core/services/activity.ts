import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Activity {
  private baseUrl = 'http://localhost:8765/ticket-service/tickets/${ticketId}/activity';

  constructor(private http: HttpClient) { }

  getTicketActivity(ticketId: string): Observable<any[]> {
      return this.http.get<any[]>(`${this.baseUrl}`);
    }
  
    logActivity(ticketId: string, actorId: string, comment: string): Observable<any> {
      return this.http.post<any>(`${this.baseUrl}/${ticketId}/comment`, null, { params: { actorId, comment } });
    }
}
