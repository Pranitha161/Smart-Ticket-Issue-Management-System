package com.smartticket.demo.controller;

import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;
import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.Assignment;
import com.smartticket.demo.enums.PRIORITY;
import com.smartticket.demo.service.implementation.AssignmentServiceImplementation;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/assignments")
public class AssignmentController {

	private final AssignmentServiceImplementation assignmentService;

	public AssignmentController(AssignmentServiceImplementation assignmentService) {
		this.assignmentService = assignmentService;
	}

	@PostMapping("/manual")
	public Mono<ResponseEntity<ApiResponse>> manualAssign(@RequestParam String ticketId, @RequestParam String agentId,
			@RequestParam PRIORITY priority, @RequestParam Long expectedVersion) {
		return assignmentService.manualAssign(ticketId, agentId, priority,expectedVersion)
				.map(assignment -> ResponseEntity
						.ok(new ApiResponse(true, "Ticket " + ticketId + " assigned to agent " + agentId)))
				.onErrorResume(OptimisticLockingFailureException.class, ex -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT) .body(new ApiResponse(false, "Ticket already updated by another manager/admin"))));
	}

	@PutMapping("/{ticketId}/complete")
	public Mono<ResponseEntity<ApiResponse>> completeAssignment(@PathVariable String ticketId) {
		return assignmentService.completeAssignment(ticketId)
				.map(assignment -> ResponseEntity
						.ok(new ApiResponse(true, "Assignment " + assignment.getId() + " marked as completed")));
				
	}

	@PutMapping("/{ticketId}/check-escalation")
	public Mono<ResponseEntity<Assignment>> checkEscalation(@PathVariable String ticketId) {
		return assignmentService.checkAndEscalate(ticketId).map(ResponseEntity::ok);
				
	}

	@PostMapping("/{ticketId}/auto")
	public Mono<ResponseEntity<Assignment>> autoAssign(@PathVariable String ticketId) {
		return assignmentService.autoAssign(ticketId).map(ResponseEntity::ok)
		;
	}

	@GetMapping("/agent-summary")
	public Mono<ResponseEntity<List<AgentSummaryDto>>> getAgentWorkloadSummary() {
		return assignmentService.assignmentsPerAgent().map(ResponseEntity::ok);
	}

	@GetMapping("/escalations/summary")
	public Mono<ResponseEntity<List<EscalationSummaryDto>>> getEscalationSummary() {
		return assignmentService.getEscalationSummary().map(ResponseEntity::ok);
	}
}
