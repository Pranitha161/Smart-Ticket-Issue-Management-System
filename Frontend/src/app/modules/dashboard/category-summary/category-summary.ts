import { ChangeDetectorRef, Component, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../core/services/dashboard';
import { LookupService } from '../../../core/services/lookup-service';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-category-summary',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './category-summary.html',
  styleUrls: ['./category-summary.css']
})
export class CategorySummary implements OnInit {
  private dashboardService = inject(DashboardService);
  private lookup = inject(LookupService);
  private cd = inject(ChangeDetectorRef);

  @ViewChild(BaseChartDirective) categoryChart?: BaseChartDirective;

  categoryChartData: ChartData<'bar'> = { labels: [], datasets: [] };

  categoryChartOptions: ChartOptions<'bar'> = {
    indexAxis: 'y',
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
      x: {
        beginAtZero: true,
        grid: { color: '#f1f5f9', drawTicks: false },
        border: { display: false },
        ticks: { stepSize: 1, color: '#94a3b8' }
      },
      y: {
        grid: { display: true, color: '#f1f5f9', tickBorderDash: [5, 5] },
        border: { display: false },
        ticks: { color: '#64748b', font: { size: 12, weight: 500 } }
      }
    }
  };

  ngOnInit(): void {
    this.dashboardService.getCategorySummary().subscribe(data => {

      this.categoryChartData = {
        labels: data.map(d => this.lookup.getCategoryName(d.categoryId)),
        datasets: [{
          label: 'Tickets',
          data: data.map(d => d.count),
          backgroundColor: '#5c67f2',
          borderRadius: 4,
          barThickness: 18
        }]
      };

      this.cd.detectChanges();
      this.categoryChart?.update();
    });
  }
}