import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { DashboardService } from '../../../core/services/dashboard';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-priority-summary',
  imports: [BaseChartDirective],
  templateUrl: './priority-summary.html',
  styleUrl: './priority-summary.css',
})
export class PrioritySummary implements OnInit{
  priorityChartData: ChartData<'pie'> = { labels: [], datasets: [] };
  priorityChartOptions: ChartOptions<'pie'> = {
      responsive: true,
      maintainAspectRatio: true,
      aspectRatio: 2
    };
  @ViewChild(BaseChartDirective) priorityChart?: BaseChartDirective;
  constructor(
    private dashboardService:DashboardService,
    private cd:ChangeDetectorRef
  ) {}
  ngOnInit(): void {
       this.dashboardService.getTicketPrioritySummary().subscribe(data => {
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
