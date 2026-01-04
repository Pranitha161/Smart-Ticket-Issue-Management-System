// import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { DashboardService } from '../../../core/services/dashboard';
// import { BaseChartDirective } from 'ng2-charts';
// import { Chart, ChartData, ChartOptions, registerables } from 'chart.js';
// import { LookupService } from '../../../core/services/lookup-service';

// Chart.register(...registerables); 

// @Component({
//   selector: 'app-category-summary',
//   standalone: true,
//   imports: [CommonModule,BaseChartDirective], 
//   templateUrl: './category-summary.html',
//   styleUrls: ['./category-summary.css']
// })
// export class CategorySummary implements OnInit {
//   categoryChartData: ChartData<'bar'> = { labels: [], datasets: [] };
//   categoryChartOptions: ChartOptions<'bar'> = { responsive: true };

//   @ViewChild(BaseChartDirective) categoryChart?: BaseChartDirective; 

//   constructor(
//     private dashboardService: DashboardService,
//     private cd: ChangeDetectorRef,
//     private lookup:LookupService
//   ) {}

//  ngOnInit(): void {
//   this.dashboardService.getCategorySummary().subscribe(data => {

//     const labels = data.map(d => this.lookup.getCategoryName(d.categoryId));
//     const counts = data.map(d => d.count);
//     console.log(data);
//     this.categoryChartData = {
//       labels: labels,
//       datasets: [
//         {
//           label: 'Tickets',
//           data: counts,
        
//           backgroundColor: [
//             '#5c67f2', 
//             '#818cf8',
//             '#c7d2fe'  
//           ],
//           borderRadius: 6,
//           barThickness: 24,
//         }
//       ]
//     };

//     this.categoryChartOptions = {
//       indexAxis: 'y', 
//       responsive: true,
//       maintainAspectRatio: false,
//       plugins: {
//         legend: { display: false },
//         tooltip: {
//           displayColors: false,
//           backgroundColor: '#1e293b',
//           padding: 10,
//           bodyFont: { size: 13}
//         }
//       },
//       scales: {
//         x: {
//           beginAtZero: true,
//           grid: { color: '#f1f5f9' },
//           border: { display: false },
//           ticks: { stepSize: 1, color: '#94a3b8' }
//         },
//         y: {
//           grid: { display: false },
//           border: { display: false },
//           ticks: { color: '#1e293b', font: {  size: 12 } }
//         }
//       }
//     };
    
//     this.cd.detectChanges();
//     this.categoryChart?.update();
//   });
// }
// }
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
  
  // Re-configured to match the clean style in image_030724
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
        grid: { display: true, color: '#f1f5f9', tickBorderDash: [5, 5] }, // Dotted lines from image_030724
        border: { display: false },
        ticks: { color: '#64748b', font: { size: 12, weight: 500 } }
      }
    }
  };

  ngOnInit(): void {
    this.dashboardService.getCategorySummary().subscribe(data => {
      // Direct mapping to ensure lookup names are captured
      this.categoryChartData = {
        labels: data.map(d => this.lookup.getCategoryName(d.categoryId)),
        datasets: [{
          label: 'Tickets',
          data: data.map(d => d.count),
          // Single color theme as seen in image_030724 (Priority Distribution)
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