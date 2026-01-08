import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { TicketService } from './ticket'; 
import { AssignmentEscalation } from './assignment-escalation';
import { LookupService } from './lookup-service'; 
import { Ticket } from '../../shared/models/ticket.model';
import { ApiResponse } from '../../shared/models/api-response.model';
import { Assignment } from '../../shared/models/assignment-escalation.model';

@Injectable({ providedIn: 'root' })
export class ticketListService {

  constructor(
    private ticketService: TicketService,
    private assignmentService: AssignmentEscalation,
    private lookup: LookupService
  ) {}

  getTickets(): Observable<Ticket[]> {
    return this.ticketService.getTickets().pipe(
      map(tickets => tickets.map(t => this.enrichTicket(t)))
    );
  }

  getTicketsByUserId(userId: string): Observable<Ticket[]> {
    return this.ticketService.getTicketsByUserId(userId).pipe(
      map(tickets => tickets.map(t => this.enrichTicket(t)))
    );
  }
  getTicketsByAgentId(userId: string): Observable<Ticket[]> {
    return this.ticketService.getTicketsByAgentId(userId).pipe(
      map(tickets => tickets.map(t => this.enrichTicket(t)))
    );
  }

  getTicketById(id: string): Observable<Ticket> {
    return this.ticketService.getTicketById(id).pipe(
      map(ticket => this.enrichTicket(ticket))
    );
  }

  reopenTicket(id: string): Observable<any> {
    return this.ticketService.reopenTicket(id);
  }

  manualAssign(ticketId: string, agentId: string, priority: string,expectedVersion: number): Observable<ApiResponse> {
    return this.assignmentService.manualAssign(ticketId, agentId, priority,expectedVersion);
  }

  autoAssign(ticketId: string): Observable<Assignment> {
    return this.assignmentService.autoAssign(ticketId);
  }
  
  closeTicket(id: string): Observable<any> {
    return this.ticketService.closeTicket(id);
  }

  
  updateTicket(id: string, ticket: any): Observable<any> {
    return this.ticketService.updateTicket(id,ticket);
  }

  private enrichTicket(ticket: any): any {
    return {
      ...ticket,
      categoryName: this.lookup.getCategoryName(ticket.categoryId),
      createdByName: this.lookup.getUserName(ticket.createdBy),
      assignedToName: this.lookup.getUserName(ticket.assignedTo)
    };
  }
}
