// import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { DashboardService } from '../../../core/services/dashboard';
// import { LookupService } from '../../../core/services/lookup-service';
// import { AgentSummaryDto } from '../../../shared/models/dashboard.model';

// @Component({
//   selector: 'app-agent-summary',
//   standalone: true,
//   imports: [CommonModule],
//   templateUrl: './agent-summary.html',
//   styleUrls: ['./agent-summary.css']
// })
// export class AgentSummary implements OnInit {
//   private dashboardService = inject(DashboardService);
//   private lookup = inject(LookupService);
//   private cd = inject(ChangeDetectorRef);

//   agentMetrics: any[] = [];

//   ngOnInit(): void {
//     this.dashboardService.getAssignmentsPerAgent().subscribe({
//       next: (res: AgentSummaryDto[]) => {
//         this.agentMetrics = res.map(item => {
//           const assigned = Number(item.assignedCount);
//           const resolved = Number(item.resolvedCount);
//           const overdue = Number(item.overdueCount);
//           const escLevel = Number(item.escalationLevel);

//           return {
//             ...item,
//             name: this.lookup.getUserName(item.agentId) || 'Unknown Agent',
//             email: this.lookup.getUserEmail(item.agentId) || 'N/A',
//             performanceScore: assigned > 0 ? Math.round((resolved / assigned) * 100) : 0,
//             isHighRisk: overdue > 5 || escLevel > 1,
//             statusLabel: overdue > 0 ? 'Action Required' : 'On Track'
//           };
//         });
//         this.agentMetrics.sort((a, b) => b.performanceScore - a.performanceScore);
//         this.cd.detectChanges();
//       },
//       error: (err) => console.error('Agent Metrics Error:', err)
//     });
//   }

//   // Simple helper to format minutes into readable hours/mins
//   formatTime(mins: number): string {
//     const h = Math.floor(mins / 60);
//     const m = Math.round(mins % 60);
//     return h > 0 ? `${h}h ${m}m` : `${m}m`;
//   }

//   private calculateScore(item: AgentSummaryDto): number {
//     if (item.assignedCount === 0) return 0;
//     return Math.round((item.resolvedCount / item.assignedCount) * 100);
//   }
// }


import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../core/services/dashboard';
import { AgentStatsDto } from '../../../shared/models/dashboard.model';
import { AdminUser } from '../../../core/services/admin-user';
import { LookupService } from '../../../core/services/lookup-service';

@Component({
  selector: 'app-agent-summary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './agent-summary.html',
  styleUrls: ['./agent-summary.css']
})
export class AgentSummary implements OnInit {
  private dashboardService = inject(DashboardService);
  private userService=inject(AdminUser);
  private cd = inject(ChangeDetectorRef);
   private lookup = inject(LookupService);
  agentStats: any[] = [];

  ngOnInit(): void {
    this.dashboardService.getAllAgentStats().subscribe({
      next: (res: AgentStatsDto[]) => {
        this.agentStats = res.map(item => {
          const assigned = Number(item.currentAssignments);
          const resolved = Number(item.resolvedCount);

          return {
            ...item,
             name: this.lookup.getUserName(item.agentId) || 'Unknown Agent',
            email: this.lookup.getUserEmail(item.agentId) || 'N/A',
            performanceScore: assigned + resolved > 0
              ? Math.round((resolved / (assigned + resolved)) * 100)
              : 0,
            statusLabel: assigned > 5 ? 'Busy' : 'Available'
          };
        });
        this.agentStats.sort((a, b) => b.performanceScore - a.performanceScore);
        console.log(this.agentStats);
        this.cd.detectChanges();
      },
      error: (err) => console.error('Agent Stats Error:', err)
    });
  }
}
