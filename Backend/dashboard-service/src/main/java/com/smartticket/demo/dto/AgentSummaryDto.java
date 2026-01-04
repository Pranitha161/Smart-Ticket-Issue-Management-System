package com.smartticket.demo.dto;

import lombok.Data;

@Data
public class AgentSummaryDto {
	private String agentId;
	private long assignedCount;
	private long resolvedCount;
	private long overdueCount;
	private int escalationLevel;
	private double averageResolutionTimeMinutes;
}
