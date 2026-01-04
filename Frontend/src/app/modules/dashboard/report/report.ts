import { CommonModule } from "@angular/common";
import { StatusSummary } from "../status-summary/status-summary";
import { PrioritySummary } from "../priority-summary/priority-summary";

import { ChangeDetectorRef, Component, inject, OnInit, ElementRef, ViewChild } from "@angular/core";
import { AgentSummary } from "../agent-summary/agent-summary";
import { EscalationSummary } from "../escalation-summary/escalation-summary";
import { CategorySummary } from "../category-summary/category-summary";
import { UserTicketStats } from "../../../shared/models/ticket.model";
import { TicketService } from "../../../core/services/ticket";
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import { DashboardService } from "../../../core/services/dashboard";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    StatusSummary,
    PrioritySummary,
    EscalationSummary,
    CategorySummary,
    AgentSummary,
    // SlaCompliance
  ],
  templateUrl: './report.html',
  styleUrls: ['./report.css']
})
export class Report implements OnInit {
  @ViewChild('reportContent') reportContent!: ElementRef;
  private dashboardService = inject(DashboardService);

  stats: UserTicketStats = { total: 0, open: 0, resolved: 0, critical: 0 };
  slaPercentage: number = 0;
  isSlaCritical: boolean = false;
  constructor(private cd: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.dashboardService.getGlobalStats().subscribe(data => {
      this.stats = data;
      this.calculateKPIs();
      this.cd.detectChanges();
    });
  }
  public async downloadPDF() {
    const element = this.reportContent.nativeElement;

    const canvas = await html2canvas(element, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#f8f9fa'
    });
    const imgData = canvas.toDataURL('image/png');
    const pdf = new jsPDF('p', 'mm', 'a4');
    const pageWidth = 210;
    const pageHeight = 297;
    const imgWidth = pageWidth;
    const imgHeight = (canvas.height * imgWidth) / canvas.width;
    let heightLeft = imgHeight;
    let position = 0;

    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
    heightLeft -= pageHeight;

    while (heightLeft >= 0) {
      position = heightLeft - imgHeight;
      pdf.addPage();
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
      heightLeft -= pageHeight;
    }
    pdf.save(`System-Report-${new Date().getTime()}.pdf`);
  }
  private calculateKPIs(): void {
    if (this.stats.total > 0) {
      this.slaPercentage = Math.round((this.stats.resolved / this.stats.total) * 100);
      this.isSlaCritical = this.slaPercentage < 80;
      this.cd.detectChanges();
    }
  }
}