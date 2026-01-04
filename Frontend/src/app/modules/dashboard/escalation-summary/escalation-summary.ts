// src/app/modules/dashboard/escalation-summary/escalation-summary.component.ts
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../core/services/dashboard'; 
import { EscalationSummaryDto } from '../../../shared/models/dashboard.model'; 

@Component({
  selector: 'app-escalation-summary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './escalation-summary.html',
  styleUrls: ['./escalation-summary.css']
})
export class EscalationSummary implements OnInit {
  private dashboardService = inject(DashboardService);
  private cd = inject(ChangeDetectorRef);

  // Using your specific DTO
  escalationSummary: EscalationSummaryDto[] = [];
  totalEscalations: number = 0;

  ngOnInit(): void {
  this.dashboardService.getEscalationSummary().subscribe({
    next: (res: EscalationSummaryDto[]) => {
      // FIX: Force conversion to Number for math operations
      this.escalationSummary = [...res].sort((a, b) => {
        return Number(b.level) - Number(a.level);
      });

      this.totalEscalations = res.reduce((acc, curr) => {
        return acc + Number(curr.count);
      }, 0);

      this.cd.detectChanges();
    }
  });
}

  // Strictly typed for your number-based DTO
  getEscalationColor(level: number): string {
    if (level >= 3) return '#ef4444'; // Red - Critical (image_03074a style)
    if (level === 2) return '#f59e0b'; // Amber - Warning
    return '#5c67f2';                // Purple - Info (Theme Primary)
  }
}