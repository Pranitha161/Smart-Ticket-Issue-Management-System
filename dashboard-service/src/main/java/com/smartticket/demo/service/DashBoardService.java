package com.smartticket.demo.service;

import java.util.Map;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DashBoardService {

	Mono<Map<String, Long>> getTicketStatusSummary();

	Mono<Map<String, Map<String, Long>>> getTicketStatusPrioritySummary();

	Flux<AgentSummaryDto> getAssignmentsPerAgent();

	Flux<EscalationSummaryDto> getEscalationSummary();

}
