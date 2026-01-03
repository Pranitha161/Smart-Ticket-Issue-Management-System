import { CommonModule } from "@angular/common";
import { StatusSummary } from "../status-summary/status-summary";
import { PrioritySummary } from "../priority-summary/priority-summary";
import { CategorySummary } from "../category-summary/category-summary";
import { SlaCompliance } from "../sla-compliance/sla-compliance";
import { Component } from "@angular/core";
import { AgentSummary } from "../agent-summary/agent-summary";
import { EscalationSummary } from "../escalation-summary/escalation-summary";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    StatusSummary,
    PrioritySummary,
    EscalationSummary,
    // CategorySummary,
    AgentSummary,
    // SlaCompliance
  ],
  templateUrl: './report.html',
  styleUrls: ['./report.css']
})
export class Report {}
