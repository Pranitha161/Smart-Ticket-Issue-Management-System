export interface Assignment {
  id: string;
  ticketId: string;
  agentId: string;
  assignedAt: string;
  dueAt?: string;
  unassignedAt?: string;
  breached: boolean;
  breachedAt?: string;
  status: string;
  type: string;
  escalationLevel: number;
}
export interface AgentSummaryDto {
  agentId: string;
  activeAssignments: number;
  completedAssignments: number;
}
export interface EscalationSummaryDto {
  level: number;
  count: number;
}
