import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PrioritySummaryDto, StatusSummaryDto, Ticket, UserTicketStats } from '../../shared/models/ticket.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private baseUrl =   `${environment.apiGatewayUrl}${environment.endpoints.tickets}/tickets`;

  constructor(private http: HttpClient) { }

  createTicket(ticket: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, ticket);
  }

  getTickets(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}`);
  }

  getTicketsByUserId(userId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/user/${userId}`);
  }

  getTicketById(id: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  updateTicket(id: string, ticket: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}`, ticket);
  }

  closeTicket(id: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/close`, {});
  }

  resolveTicket(id: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/resolve`, {});
  }

  reopenTicket(id: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/reopen`, {});
  }

  deleteTicket(id: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }

  getUserStats(userId: string): Observable<UserTicketStats> {
    return this.http.get<UserTicketStats>(`${this.baseUrl}/user/${userId}/stats`);
  }

  getGlobalStats(): Observable<UserTicketStats> { 
    return this.http.get<UserTicketStats>(`${this.baseUrl}/user/stats`); 
  }

  getStatusSummary(): Observable<StatusSummaryDto[]> {
    return this.http.get<StatusSummaryDto[]>(`${this.baseUrl}/status-summary`);
  }

  getPrioritySummary(): Observable<PrioritySummaryDto[]> {
    return this.http.get<PrioritySummaryDto[]>(`${this.baseUrl}/status-priority-summary`);
  }
  getRecentTickets(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.baseUrl}/recent`);
  }

  getRecentTicketsByUser(userId: string): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.baseUrl}/recent/${userId}`);
  }

  getRecentTicketsByAgent(agentId: string): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.baseUrl}/recent/agent/${agentId}`);
  }

  getTicketsByAgentId(agentId: string): Observable<Ticket[]> {
  return this.http.get<Ticket[]>(`${this.baseUrl}/agent/${agentId}`);
}

getAgentStats(agentId: string): Observable<UserTicketStats> {
    return this.http.get<UserTicketStats>(`${this.baseUrl}/agent/${agentId}/stats`);
  }



}
