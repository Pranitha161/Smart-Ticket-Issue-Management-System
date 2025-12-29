package com.smartticket.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.Assignment;
import com.smartticket.demo.enums.PRIORITY;
import com.smartticket.demo.service.AssignmentService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/assignments")
public class AssignmentController {

	private final AssignmentService assignmentService;

	public AssignmentController(AssignmentService assignmentService) {
		this.assignmentService = assignmentService;
	}

	@PostMapping("/manual")
	public Mono<ResponseEntity<ApiResponse>> manualAssign(@RequestParam String ticketId, @RequestParam String agentId,
			@RequestParam PRIORITY priority) {
		return assignmentService.manualAssign(ticketId, agentId, priority)
				.map(assignment -> ResponseEntity
						.ok(new ApiResponse(true, "Ticket " + ticketId + " assigned to agent " + agentId)))
				.onErrorResume(e -> {
					String message = e.getMessage();
					return Mono.just(ResponseEntity.badRequest()
							.body(new ApiResponse(false, "Manual assignment failed: " + message)));
				});
	}

	@PutMapping("/{ticketId}/complete")
	public Mono<ResponseEntity<ApiResponse>> completeAssignment(@PathVariable String ticketId) {
		return assignmentService.completeAssignment(ticketId)
				.map(assignment -> ResponseEntity
						.ok(new ApiResponse(true, "Assignment " + assignment.getId() + " marked as completed")))
				.onErrorResume(e -> {
					String message = e.getMessage();
					if (message.contains("not found")) {
						return Mono.just(ResponseEntity.status(404).body(new ApiResponse(false, message)));
					} else if (message.contains("already completed")) {
						return Mono.just(ResponseEntity.status(400).body(new ApiResponse(false, message)));
					} else if (message.contains("escalated")) {
						return Mono.just(ResponseEntity.status(400).body(new ApiResponse(false, message)));
					} else {
						return Mono.just(ResponseEntity.status(500)
								.body(new ApiResponse(false, "Unexpected error: " + message)));
					}
				});
	}

	@PutMapping("/{ticketId}/check-escalation")
	public Mono<ResponseEntity<Assignment>> checkEscalation(@PathVariable String ticketId) {
		return assignmentService.checkAndEscalate(ticketId).map(ResponseEntity::ok)
				.onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
	}

	@PostMapping("/{ticketId}/auto")
	public Mono<ResponseEntity<Assignment>> autoAssign(@PathVariable String ticketId) {
		return assignmentService.autoAssign(ticketId).map(ResponseEntity::ok)
				.onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
	}
}
