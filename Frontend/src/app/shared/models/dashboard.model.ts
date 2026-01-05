export interface StatusSummaryDto {
    status: string;
    count: number;
}
export interface PrioritySummaryDto {
    priority: string;
    count: number;
}
export interface CategorySummaryDto {
    categoryId: string;
    count: number;
}
export interface AgentSummaryDto {
    agentId: string;
    assignedCount: number;
    resolvedCount: number;
    overdueCount: number;
    escalationLevel: number;
    averageResolutionTimeMinutes: number;
}
export interface EscalationSummaryDto {
    level: string;
    count: number;
}

export interface AgentStatsDto {
    agentId: string; agentLevel: string;
    currentAssignments: number;
    resolvedCount: number;
    resolutionRate: number;
}