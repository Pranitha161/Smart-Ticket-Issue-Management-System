// src/app/modules/dashboard/status-summary/status-summary.component.ts
import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../core/services/dashboard';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartData, ChartOptions, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-status-summary',
  standalone: true,
  imports: [CommonModule,BaseChartDirective],
  templateUrl: './status-summary.html',
  styleUrls: ['./status-summary.css']
})
export class StatusSummary implements OnInit {
  statusChartData: ChartData<'bar'> = { labels: [], datasets: [] };
    statusChartOptions: ChartOptions<'bar'> = { responsive: true };
   @ViewChild(BaseChartDirective) statusChart?: BaseChartDirective;
  constructor(private dashboardService: DashboardService,private cd:ChangeDetectorRef) { }

  ngOnInit(): void {
     this.dashboardService.getTicketStatusSummary().subscribe(data => {
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
    
  }
}
