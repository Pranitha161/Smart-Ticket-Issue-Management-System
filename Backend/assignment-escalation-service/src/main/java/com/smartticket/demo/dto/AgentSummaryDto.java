package com.smartticket.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentSummaryDto {
	private String agentId;
	private long assignedCount;
	private long resolvedCount;
	private long overdueCount;
	private int escalationLevel;
	private double averageResolutionTimeMinutes;
}
