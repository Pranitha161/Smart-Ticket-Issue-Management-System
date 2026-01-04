import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AgentSummaryDto, Assignment, EscalationSummaryDto } from '../../shared/models/assignment-escalation.model';
import { ApiResponse } from '../../shared/models/api-response.model';
import { environment } from '../../../environments/environment';



@Injectable({ providedIn: 'root' })
export class AssignmentEscalation {
  
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.assignments}/assignments`;

  constructor(private http: HttpClient) {}

  manualAssign(ticketId: string, agentId: string, priority: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.baseUrl}/manual?ticketId=${ticketId}&agentId=${agentId}&priority=${priority}`, {}
    );
  }

  autoAssign(ticketId: string): Observable<Assignment> {
    return this.http.post<Assignment>(`${this.baseUrl}/${ticketId}/auto`, {});
  }

  completeAssignment(ticketId: string): Observable<ApiResponse> {
    return this.http.put<ApiResponse>(`${this.baseUrl}/${ticketId}/complete`, {});
  }

  checkEscalation(ticketId: string): Observable<Assignment> {
    return this.http.put<Assignment>(`${this.baseUrl}/${ticketId}/check-escalation`, {});
  }

  getAgentSummary(): Observable<AgentSummaryDto[]> {
    return this.http.get<AgentSummaryDto[]>(`${this.baseUrl}/agent-summary`);
  }

  getEscalationSummary(): Observable<EscalationSummaryDto[]> {
    return this.http.get<EscalationSummaryDto[]>(`${this.baseUrl}/escalations/summary`);
  }
}
