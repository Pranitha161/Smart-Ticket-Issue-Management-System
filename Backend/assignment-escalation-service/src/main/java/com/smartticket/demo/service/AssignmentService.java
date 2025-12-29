package com.smartticket.demo.service;

import com.smartticket.demo.entity.Assignment;
import com.smartticket.demo.enums.PRIORITY;

import reactor.core.publisher.Mono;

public interface AssignmentService {
	
	Mono<Assignment> manualAssign(String ticketId, String agentId,PRIORITY priority);
	
	Mono<Assignment> autoAssign(String ticketId);

	Mono<Assignment> completeAssignment(String ticketId);

	Mono<Assignment> checkAndEscalate(String ticketId);
}
