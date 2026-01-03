import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PrioritySummaryDto, StatusSummaryDto } from '../../shared/models/ticket.model';
import { AgentSummaryDto, CategorySummaryDto, EscalationSummaryDto } from '../../shared/models/dashboard.model';
import { environment } from '../../../environments/environment';

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

  getAgentSummary(): Observable<AgentSummaryDto[]> {
    return this.http.get<AgentSummaryDto[]>(`${this.baseUrl}/assignments/agent-summary`);
  }

  getEscalationSummary(): Observable<EscalationSummaryDto[]> {
    return this.http.get<EscalationSummaryDto[]>(`${this.baseUrl}/assignments/escalation-summary`);
  }
}

