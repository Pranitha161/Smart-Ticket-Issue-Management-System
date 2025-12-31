import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private baseUrl = 'http://localhost:8765/ticket-service/tickets';

  constructor(private http: HttpClient) {}

  // Create Ticket
  createTicket(ticket: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, ticket);
  }

  // Get all tickets
  getTickets(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}`);
  }

  // Get tickets by userId
  getTicketsByUserId(userId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/user/${userId}`);
  }

  // Get ticket by id
  getTicketById(id: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  // Update ticket
  updateTicket(id: string, ticket: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}`, ticket);
  }

  // Close ticket
  closeTicket(id: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/close`, {});
  }

  // Resolve ticket
  resolveTicket(id: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/resolve`, {});
  }

  // Reopen ticket
  reopenTicket(id: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}/reopen`, {});
  }

  // Delete ticket
  deleteTicket(id: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
