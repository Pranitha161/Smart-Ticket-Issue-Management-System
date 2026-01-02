package com.smartticket.demo.dto;

import java.util.Set;

import com.smartticket.demo.enums.AGENT_LEVEL;

import lombok.Data;

@Data
public class AgentDto {
	private String id;
	private String username;
	private String email;
	private AGENT_LEVEL agentLevel;
	private String category;
	private Set<String> skills;
	private int currentAssignments;
	private int resolvedCount;
}
