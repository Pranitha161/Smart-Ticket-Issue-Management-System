import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketService } from '../../../core/services/ticket';
import { UserTicketStats } from '../../../shared/models/ticket.model';
import { AuthService } from '../../../core/services/auth';
import { TicketList } from '../../tickets/ticket-list/ticket-list';
import { RecentTickets } from '../../tickets/recent-tickets/recent-tickets';

import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartData, ChartOptions, registerables } from 'chart.js';
import { StatusSummary } from '../status-summary/status-summary';
import { PrioritySummary } from '../priority-summary/priority-summary';
import { AssignmentEscalation } from '../../../core/services/assignment-escalation';
import { EscalationSummaryDto } from '../../../shared/models/assignment-escalation.model';

Chart.register(...registerables);

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, RecentTickets, StatusSummary, PrioritySummary],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class Dashboard implements OnInit {
  stats: UserTicketStats = { total: 0, open: 0, resolved: 0, critical: 0 };

  statusChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  priorityChartData: ChartData<'pie'> = { labels: [], datasets: [] };

  statusChartOptions: ChartOptions<'bar'> = { responsive: true };
  priorityChartOptions: ChartOptions<'pie'> = {
    responsive: true,
    maintainAspectRatio: true,
    aspectRatio: 2
  };

  role: string = '';
  breachedCount = 0;

  @ViewChild(BaseChartDirective) statusChart?: BaseChartDirective;
  @ViewChild(BaseChartDirective) priorityChart?: BaseChartDirective;

  constructor(
    private ticketService: TicketService,
    private authService: AuthService,
    private assignmentService:AssignmentEscalation,
    private cd: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    const userId = this.authService.userId()!;
    this.role = this.authService.roles()[0];
    

      if (this.role === 'ADMIN' || this.role === 'MANAGER') {
  // ✅ Admin/Manager stats
  this.ticketService.getGlobalStats().subscribe(data => {
    this.stats = data;
    this.cd.detectChanges();
  });

  
} 
else if (this.role === 'AGENT') {
  // ✅ Agent stats
  this.ticketService.getAgentStats(userId).subscribe(data => {
    this.stats = data;
    this.cd.detectChanges();
  });

 
} 
else {
  // ✅ Normal user stats
  this.ticketService.getUserStats(userId).subscribe(data => {
    this.stats = data;
    this.cd.detectChanges();
  });
}


      if (this.role === 'ADMIN' || this.role === 'MANAGER') {
        this.ticketService.getStatusSummary().subscribe(data => {
          this.statusChartData.labels = data.map(d => d.status);
          this.statusChartData.datasets = [
            {
              label: 'Tickets',
              data: data.map(d => d.count),
              backgroundColor: ['#42A5F5', '#66BB6A', '#FFA726']
            }
          ];
          this.statusChart?.update();
        });

        this.ticketService.getPrioritySummary().subscribe(data => {
          this.priorityChartData.labels = data.map(d => d.priority);
          this.priorityChartData.datasets = [
            {
              data: data.map(d => d.count),
              backgroundColor: ['#EF5350', '#AB47BC', '#29B6F6', '#FFCA28']
            }
          ];
          this.priorityChart?.update();
        });
      }
    }
  }
