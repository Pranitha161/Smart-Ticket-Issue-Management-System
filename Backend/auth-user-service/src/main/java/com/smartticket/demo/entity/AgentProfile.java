package com.smartticket.demo.entity;

import java.util.Set;

import com.smartticket.demo.enums.AGENT_LEVEL;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentProfile {
	private AGENT_LEVEL agentLevel;
	private String categoryId;
	private Set<String> skills;
	private int currentAssignments;
	private int resolvedCount;
	private int escalatedCount;

}
