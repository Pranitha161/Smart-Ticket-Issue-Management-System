// src/app/modules/dashboard/escalation-summary/escalation-summary.component.ts
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
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
  escalationSummary: EscalationSummaryDto[] = [];

  constructor(private dashboardService: DashboardService,private cd:ChangeDetectorRef) {}

  ngOnInit(): void {
    this.dashboardService.getEscalationSummary()
      .subscribe(res =>{ this.escalationSummary = res;
        console.log(res);
        this.cd.detectChanges();
      });
  }
}
