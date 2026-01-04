import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PrioritySummaryDto, StatusSummaryDto, UserTicketStatsDto } from '../../shared/models/ticket.model';
import { AgentSummaryDto, CategorySummaryDto, EscalationSummaryDto } from '../../shared/models/dashboard.model';
import { environment } from '../../../environments/environment';
import { UserStatsDto } from '../../shared/models/authuser.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {

  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.dashboards}/dashboard`;

  constructor(private http: HttpClient) { }

  getTicketStatusSummary(): Observable<StatusSummaryDto[]> {
    return this.http.get<StatusSummaryDto[]>(`${this.baseUrl}/tickets/status-summary`);
  }

  getTicketPrioritySummary(): Observable<PrioritySummaryDto[]> {
    return this.http.get<PrioritySummaryDto[]>(`${this.baseUrl}/tickets/status-priority-summary`);
  }

  getCategorySummary(): Observable<CategorySummaryDto[]> {
    return this.http.get<CategorySummaryDto[]>(`${this.baseUrl}/tickets/category-summary`);
  }

  getUserStats(userId: string): Observable<UserTicketStatsDto> {
    return this.http.get<UserTicketStatsDto>(`${this.baseUrl}/tickets/user/${userId}/stats`);
  }
  getAgentStats(agentId: string): Observable<UserTicketStatsDto> {
    return this.http.get<UserTicketStatsDto>(`${this.baseUrl}/tickets/agent/${agentId}/stats`);
  }
  getGlobalStats(): Observable<UserTicketStatsDto> { return this.http.get<UserTicketStatsDto>(`${this.baseUrl}/tickets/global-stats`); }

  getStats(): Observable<UserStatsDto> {
    return this.http.get<UserStatsDto>(`${this.baseUrl}/users/stats`);
  }

  getAssignmentsPerAgent(): Observable<AgentSummaryDto[]> {
    return this.http.get<AgentSummaryDto[]>(`${this.baseUrl}/assignments/agent-summary`);
  }

  getEscalationSummary(): Observable<EscalationSummaryDto[]> {
    return this.http.get<EscalationSummaryDto[]>(`${this.baseUrl}/assignments/escalation-summary`);
  }
}

