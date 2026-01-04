package com.smartticket.demo.service;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.CategorySummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.dto.UserStatsDto;
import com.smartticket.demo.dto.UserTicketStatsDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DashBoardService {

	Flux<StatusSummaryDto> getTicketStatusSummary();

	Flux<PrioritySummaryDto> getTicketStatusPrioritySummary();

	Flux<AgentSummaryDto> getAssignmentsPerAgent();

	Flux<EscalationSummaryDto> getEscalationSummary();

	Flux<CategorySummaryDto> getCategorySummary();

	Mono<UserTicketStatsDto> getGlobalStats();

	Mono<UserTicketStatsDto> getUserStats(String userId);

	Mono<UserTicketStatsDto> getAgentStats(String agentId);

	Mono<UserStatsDto> getStats();

}
