package com.smartticket.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.service.implementation.DashBoardServiceImplementation;

import reactor.core.publisher.Flux;

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

	@GetMapping("/assignments/agent-summary")
	public Flux<AgentSummaryDto> getAssignmentsPerAgent() {
		return dashboardService.getAssignmentsPerAgent();
	}

	@GetMapping("/assignments/agent-summary")
	public Flux<EscalationSummaryDto> getEscalationSummary() {
		return dashboardService.getEscalationSummary();
	}

}
