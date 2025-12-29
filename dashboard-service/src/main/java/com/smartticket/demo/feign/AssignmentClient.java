package com.smartticket.demo.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;

@FeignClient(name = "assignment-escalation-service")
public interface AssignmentClient {
    @GetMapping("/assignments/agent-summary")
    List<AgentSummaryDto> getAssignmentsPerAgent();
    
    @GetMapping("/assignments/escalations/summary")
	List<EscalationSummaryDto> getEscalationSummary();
}

