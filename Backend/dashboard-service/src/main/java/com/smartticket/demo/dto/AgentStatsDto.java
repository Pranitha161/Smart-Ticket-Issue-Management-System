package com.smartticket.demo.dto;


import com.smartticket.demo.enums.AGENT_LEVEL;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentStatsDto {
    private String agentId;         
    private AGENT_LEVEL agentLevel; 
    private int currentAssignments; 
    private int resolvedCount;       
    private double resolutionRate;  
}
