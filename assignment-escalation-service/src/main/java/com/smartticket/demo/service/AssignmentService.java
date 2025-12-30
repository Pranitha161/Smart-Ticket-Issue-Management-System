package com.smartticket.demo.service;

import java.util.List;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;
import com.smartticket.demo.entity.Assignment;
import com.smartticket.demo.enums.PRIORITY;

import reactor.core.publisher.Mono;

public interface AssignmentService {
	
	Mono<Assignment> manualAssign(String ticketId, String agentId,PRIORITY priority);
	
	Mono<Assignment> autoAssign(String ticketId);

	Mono<Assignment> completeAssignment(String ticketId);

	Mono<Assignment> checkAndEscalate(String ticketId);

	Mono<List<AgentSummaryDto>> assignmentsPerAgent();

	Mono<List<EscalationSummaryDto>> getEscalationSummary();

	Mono<Assignment> reassign(String ticketId, String newAgentId);
}
