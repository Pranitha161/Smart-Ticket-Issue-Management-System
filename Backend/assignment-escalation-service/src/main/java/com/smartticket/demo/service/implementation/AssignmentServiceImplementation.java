package com.smartticket.demo.service.implementation;

import java.time.Duration;
import java.time.Instant;

import com.smartticket.demo.client.TicketClient;
import com.smartticket.demo.entity.Assignment;
import com.smartticket.demo.enums.ASSIGNMENT_STATUS;
import com.smartticket.demo.enums.ASSIGNMENT_TYPE;
import com.smartticket.demo.enums.PRIORITY;
import com.smartticket.demo.enums.STATUS;
import com.smartticket.demo.repository.AssignmentRepository;
import com.smartticket.demo.repository.SlaRuleRepository;
import com.smartticket.demo.service.AssignmentService;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AssignmentServiceImplementation implements AssignmentService{
	
	private final AssignmentRepository assignmentRepo;
	private final SlaRuleRepository slaRuleRepo;
	private final TicketClient ticketClient;
	
	AssignmentServiceImplementation(AssignmentRepository assignmentRepo,SlaRuleRepository slaRuleRepo,TicketClient ticketClient){
		this.assignmentRepo=assignmentRepo;
		this.slaRuleRepo=slaRuleRepo;
		this.ticketClient=ticketClient;
	}
	

	@Override
	public Mono<Assignment> manualAssign(String ticketId, String agentId, PRIORITY priority) {
		return ticketClient.getTicketById(ticketId)
		        .filter(ticket -> ticket.getStatus() != STATUS.CLOSED)
		        .switchIfEmpty(Mono.error(new RuntimeException("Ticket is not assignable")))
		        .flatMap(ticket -> slaRuleRepo.findByPriority(priority))
		        .switchIfEmpty(Mono.error(new RuntimeException("No SLA rule for " + priority)))
		        .flatMap(rule -> {
		            Instant now = Instant.now();
		            Assignment assignment = Assignment.builder()
		                    .ticketId(ticketId)
		                    .agentId(agentId)
		                    .assignedAt(now)
		                    .dueAt(now.plus(Duration.ofMinutes(rule.getResolutionMinutes())))
		                    .status(ASSIGNMENT_STATUS.ASSIGNED) 
		                    .type(ASSIGNMENT_TYPE.MANUAL)
		                    .breached(false)
		                    .escalationLevel(0)
		                    .build();
		            return assignmentRepo.save(assignment);
		        });

	   
	}

	@Override
	public Mono<Assignment> completeAssignment(String ticketId) {
	    return assignmentRepo.findTopByTicketIdOrderByAssignedAtDesc(ticketId)
	            .switchIfEmpty(Mono.error(new RuntimeException("Assignment not found")))
	            .flatMap(assignment -> {
	                assignment.setStatus(ASSIGNMENT_STATUS.COMPLETED);
	                assignment.setUnassignedAt(Instant.now());
	                return assignmentRepo.save(assignment);
	            });
	}
	@PostConstruct
	public void scheduleEscalationChecks() {
	    Flux.interval(Duration.ofMinutes(5))
	        .flatMap(tick -> assignmentRepo.findByStatus(ASSIGNMENT_STATUS.ASSIGNED))
	        .flatMap(this::checkAndEscalate)
	        .subscribe();
	}

	private Mono<Assignment> checkAndEscalate(Assignment assignment) {
	    Instant now = Instant.now();
	    if (assignment.getDueAt() != null && now.isAfter(assignment.getDueAt())) {
	        assignment.setBreached(true);
	        assignment.setBreachedAt(now);
	        assignment.setStatus(ASSIGNMENT_STATUS.ESCALATED);
	        assignment.setEscalationLevel(assignment.getEscalationLevel() + 1);
	        return assignmentRepo.save(assignment);
//	                .doOnSuccess(this::notifyEscalation);
	    }
	    return Mono.just(assignment);
	}



	@Override
	public Mono<Assignment> checkAndEscalate(String ticketId) {
	    return assignmentRepo.findTopByTicketIdOrderByAssignedAtDesc(ticketId)
	            .switchIfEmpty(Mono.error(new RuntimeException("Assignment not found")))
	            .flatMap(assignment -> {
	                Instant now = Instant.now();
	                if (assignment.getDueAt() != null && now.isAfter(assignment.getDueAt())) {
	                    assignment.setBreached(true);
	                    assignment.setBreachedAt(now);
	                    assignment.setStatus(ASSIGNMENT_STATUS.ESCALATED);
	                    assignment.setEscalationLevel(assignment.getEscalationLevel() + 1);
	                    return assignmentRepo.save(assignment);
	                }
	                return Mono.just(assignment);
	            });
	}


	@Override
	public Mono<Assignment> autoAssign(String ticketId) {
		// TODO Auto-generated method stub
		return null;
	}


//	@Override
//	public Mono<Assignment> autoAssign(String ticketId) {
//	    return ticketClient.getTicketById(ticketId)
//	            .filter(ticket -> ticket.getStatus() != STATUS.CLOSED)
//	            .switchIfEmpty(Mono.error(new RuntimeException("Ticket is not assignable")))
//	            .flatMap(ticket -> slaRuleRepo.findByPriority(ticket.getPriority())
//	                .switchIfEmpty(Mono.error(new RuntimeException("No SLA rule for " + ticket.getPriority())))
//	                .flatMap(rule -> {
//	                    Instant now = Instant.now();
//
//	                    // Agent selection strategy based on priority
//	                    String agentId;
//	                    switch (ticket.getPriority()) {
//	                        case HIGH:
//	                            agentId = pickSeniorAgent(); // e.g., least workload senior
//	                            break;
//	                        case MEDIUM:
//	                            agentId = pickBalancedAgent(); // e.g., round‑robin
//	                            break;
//	                        case LOW:
//	                            agentId = pickJuniorAgent(); // e.g., first available junior
//	                            break;
//	                        default:
//	                            agentId = "agent123"; // fallback
//	                    }
//
//	                    Assignment assignment = Assignment.builder()
//	                            .ticketId(ticketId)
//	                            .agentId(agentId)
//	                            .assignedAt(now)
//	                            .dueAt(now.plus(Duration.ofMinutes(rule.getResolutionMinutes())))
//	                            .status(ASSIGNMENT_STATUS.ASSIGNED)
//	                            .type(ASSIGNMENT_TYPE.RULE_BASED)
//	                            .breached(false)
//	                            .escalationLevel(0)
//	                            .build();
//
//	                    return assignmentRepo.save(assignment);
//	                }));
//	}

}
