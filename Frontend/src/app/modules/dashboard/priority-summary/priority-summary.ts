import { ChangeDetectorRef, Component, OnInit, ViewChild, inject } from '@angular/core';
import { DashboardService } from '../../../core/services/dashboard';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-priority-summary',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './priority-summary.html',
  styleUrl: './priority-summary.css',
})
export class PrioritySummary implements OnInit {
  private dashboardService = inject(DashboardService);
  private cd = inject(ChangeDetectorRef);

  @ViewChild(BaseChartDirective) priorityChart?: BaseChartDirective;

  priorityChartData: ChartData<'bar'> = { labels: [], datasets: [] };

  priorityChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#1e293b',
        padding: 12,
        cornerRadius: 8
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        grid: { color: '#f1f5f9', drawTicks: false },
        border: { display: false },
        ticks: { stepSize: 1, color: '#94a3b8' }
      },
      x: {
        grid: { display: false },
        border: { display: false },
        ticks: { color: '#1e293b', font: { weight: 'bold' } }
      }
    }
  };

  ngOnInit(): void {
    this.dashboardService.getTicketPrioritySummary().subscribe(data => {
      this.priorityChartData = {
        labels: data.map(d => d.priority),
        datasets: [{
          data: data.map(d => d.count),
          backgroundColor: data.map(d => {
            const p = d.priority.toLowerCase();
            if (p === 'critical') return '#ef4444';
            if (p === 'high') return '#f59e0b';
            if (p === 'medium') return '#5c67f2';
            return '#94a3b8';
          }),
          borderRadius: 6,
          barThickness: 40
        }]
      };

      this.cd.detectChanges();
      this.priorityChart?.update();
    });
  }
}