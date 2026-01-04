package com.smartticket.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.CategorySummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.dto.UserStatsDto;
import com.smartticket.demo.dto.UserTicketStatsDto;
import com.smartticket.demo.service.implementation.DashBoardServiceImplementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/dashboard")
public class DashBoardController {

	private final DashBoardServiceImplementation dashboardService;

	DashBoardController(DashBoardServiceImplementation dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/tickets/status-summary")
	public Flux<StatusSummaryDto> getTicketStatusSummary() {
		return dashboardService.getTicketStatusSummary();
	}

	@GetMapping("/tickets/status-priority-summary")
	public Flux<PrioritySummaryDto> getTicketStatusPrioritySummary() {
		return dashboardService.getTicketStatusPrioritySummary();
	}
	
	@GetMapping("/tickets/category-summary")
	public Flux<CategorySummaryDto> getCategorySummary() {
		return dashboardService.getCategorySummary();
	}
	
	@GetMapping("/tickets/user/{userId}/stats")
	public Mono<UserTicketStatsDto> getUserStats(@PathVariable String userId) {
		return dashboardService.getUserStats(userId);
	}
	
	@GetMapping("/tickets/agent/{agentId}/stats")
	public Mono<UserTicketStatsDto> getAgentStats(@PathVariable String agentId) {
		return dashboardService.getAgentStats(agentId);
	}
	
	@GetMapping("/tickets/global-stats")
	public Mono<UserTicketStatsDto> getGlobalStats() {
		return dashboardService.getGlobalStats();
	}
	
	@GetMapping("/users/stats")
	public Mono<UserStatsDto> getStats() {
		return dashboardService.getStats();
	}
	
	@GetMapping("/assignments/agent-summary")
	public Flux<AgentSummaryDto> getAssignmentsPerAgent() {
		return dashboardService.getAssignmentsPerAgent();
	}

	@GetMapping("/assignments/escalation-summary")
	public Flux<EscalationSummaryDto> getEscalationSummary() {
		return dashboardService.getEscalationSummary();
	}

}
