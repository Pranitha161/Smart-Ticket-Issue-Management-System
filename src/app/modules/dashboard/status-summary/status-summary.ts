import { ChangeDetectorRef, Component, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../core/services/dashboard';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-status-summary',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './status-summary.html',
  styleUrls: ['./status-summary.css']
})
export class StatusSummary implements OnInit {
  private dashboardService = inject(DashboardService);
  private cd = inject(ChangeDetectorRef);
  
  @ViewChild(BaseChartDirective) statusChart?: BaseChartDirective;

  // Define colors explicitly for the template to use
  statusColors: string[] = ['#5c67f2', '#10b981', '#f59e0b', '#ef4444'];
  
  statusChartData: ChartData<'doughnut'> = {
    labels: [],
    datasets: []
  };

  statusChartOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '80%',
    plugins: {
      legend: { display: false },
      tooltip: { backgroundColor: '#1e293b' }
    }
  };

  summaryStats = { total: 0, resolvedRate: 0 };

  ngOnInit(): void {
    this.dashboardService.getTicketStatusSummary().subscribe(data => {
      const total = data.reduce((acc, curr) => acc + curr.count, 0);
      const resolved = data.find(d => d.status.toLowerCase() === 'resolved')?.count || 0;

      this.summaryStats = {
        total: total,
        resolvedRate: total > 0 ? Math.round((resolved / total) * 100) : 0
      };

      this.statusChartData = {
        labels: data.map(d => d.status),
        datasets: [{
          data: data.map(d => d.count),
          backgroundColor: this.statusColors, // Use the array here
          hoverOffset: 4,
          borderWidth: 0
        }]
      };

      this.cd.detectChanges();
      this.statusChart?.update();
    });
  }
}