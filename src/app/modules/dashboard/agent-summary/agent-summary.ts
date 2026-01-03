// src/app/modules/dashboard/agent-summary/agent-summary.component.ts
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../core/services/dashboard'; 
import { AgentSummaryDto } from '../../../shared/models/dashboard.model'; 

@Component({
  selector: 'app-agent-summary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './agent-summary.html',
  styleUrls: ['./agent-summary.css']
})
export class AgentSummary implements OnInit {
  agentSummary: AgentSummaryDto[] = [];

  constructor(private dashboardService: DashboardService,private cd:ChangeDetectorRef) {}

  ngOnInit(): void {
    this.dashboardService.getAgentSummary()
      .subscribe(res =>{ this.agentSummary = res;this.cd.detectChanges();});
  }
}
