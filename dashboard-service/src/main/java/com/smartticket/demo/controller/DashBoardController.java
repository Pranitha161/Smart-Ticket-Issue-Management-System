package com.smartticket.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;
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
	public Mono<ResponseEntity<Map<String, Long>>> getTicketStatusSummary() {
		return dashboardService.getTicketStatusSummary().map(ResponseEntity::ok);
	}

	@GetMapping("/tickets/status-priority-summary")
	public Mono<ResponseEntity<Map<String, Map<String, Long>>>> getTicketStatusPrioritySummary() {
		return dashboardService.getTicketStatusPrioritySummary().map(ResponseEntity::ok);
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
