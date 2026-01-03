export interface StatusSummaryDto { 
    status: string; 
    count: number; 
} 
export interface PrioritySummaryDto { 
    priority: string; 
    count: number; 
} 
export interface CategorySummaryDto { 
    category: string; 
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