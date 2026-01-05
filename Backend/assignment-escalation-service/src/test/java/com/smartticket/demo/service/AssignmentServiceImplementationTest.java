package com.smartticket.demo.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.OptimisticLockingFailureException;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;
import com.smartticket.demo.dto.TicketDto;
import com.smartticket.demo.entity.Assignment;
import com.smartticket.demo.entity.SlaRule;
import com.smartticket.demo.enums.ASSIGNMENT_STATUS;
import com.smartticket.demo.enums.PRIORITY;
import com.smartticket.demo.enums.STATUS;
import com.smartticket.demo.feign.TicketClient;
import com.smartticket.demo.feign.UserClient;
import com.smartticket.demo.producer.AssignmentEscalationEventProducer;
import com.smartticket.demo.repository.AssignmentRepository;
import com.smartticket.demo.repository.SlaRuleRepository;
import com.smartticket.demo.service.implementation.AssignmentServiceImplementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AssignmentServiceImplementationTest {
	@Mock
	private AssignmentRepository assignmentRepo;
	@Mock
	private SlaRuleRepository slaRuleRepo;
	@Mock
	private TicketClient ticketClient;
	@Mock
	private UserClient userClient;
	@Mock
	private AssignmentEscalationEventProducer eventProducer;
	@Mock
	private RuleEngine ruleEngine;
	@InjectMocks
	private AssignmentServiceImplementation service;
	private Assignment assignment;

	@BeforeEach
	void setup() {
		assignment = Assignment.builder().ticketId("T1").agentId("A1").assignedAt(Instant.now())
				.status(ASSIGNMENT_STATUS.ASSIGNED).escalationLevel(0).build();

	}

	@Test
	void manualAssign_success() {
		TicketDto ticket = new TicketDto();
		ticket.setId("T1");
		ticket.setStatus(STATUS.OPEN);
		ticket.setPriority(PRIORITY.HIGH);
		ticket.setVersion(1L);

		SlaRule rule = new SlaRule();
		rule.setPriority(PRIORITY.HIGH);
		rule.setResolutionMinutes(30);

		when(ticketClient.getTicketById("T1")).thenReturn(ticket);
		when(slaRuleRepo.findByPriority(PRIORITY.HIGH)).thenReturn(Mono.just(rule));
		when(assignmentRepo.save(any())).thenReturn(Mono.just(assignment));

		StepVerifier.create(service.manualAssign("T1", "A1", PRIORITY.HIGH, 1L))
				.expectNextMatches(a -> a.getTicketId().equals("T1") && a.getAgentId().equals("A1")).verifyComplete();

		verify(eventProducer).publishAssignmentEvent("T1", "A1", "ASSIGNED");
		verify(userClient).incrementAssignments("A1");
		verify(ticketClient).assignTicket("T1", "A1");
	}

	@Test
	void manualAssign_ticketClosed() {
		TicketDto ticket = new TicketDto();
		ticket.setId("T1");
		ticket.setStatus(STATUS.CLOSED);
		ticket.setPriority(PRIORITY.HIGH);
		ticket.setVersion(1L);

		when(ticketClient.getTicketById("T1")).thenReturn(ticket);

		StepVerifier.create(service.manualAssign("T1", "A1", PRIORITY.HIGH, 1L))
				.expectErrorMatches(e -> e.getMessage().contains("Ticket is already closed")).verify();
	}

	@Test
	void manualAssign_ticketAlreadyAssigned() {
		TicketDto ticket = new TicketDto();
		ticket.setId("T1");
		ticket.setStatus(STATUS.ASSIGNED);
		ticket.setPriority(PRIORITY.HIGH);
		ticket.setVersion(1L);

		when(ticketClient.getTicketById("T1")).thenReturn(ticket);

		StepVerifier.create(service.manualAssign("T1", "A1", PRIORITY.HIGH, 1L))
				.expectErrorMatches(e -> e.getMessage().contains("already assigned")).verify();
	}

	@Test
	void manualAssign_versionMismatch() {
		TicketDto ticket = new TicketDto();
		ticket.setId("T1");
		ticket.setStatus(STATUS.OPEN);
		ticket.setPriority(PRIORITY.HIGH);
		ticket.setVersion(2L);

		when(ticketClient.getTicketById("T1")).thenReturn(ticket);

		StepVerifier.create(service.manualAssign("T1", "A1", PRIORITY.HIGH, 1L))
				.expectError(OptimisticLockingFailureException.class).verify();
	}

	@Test
	void manualAssign_noSlaRule() {
		TicketDto ticket = new TicketDto();
		ticket.setId("T1");
		ticket.setStatus(STATUS.OPEN);
		ticket.setPriority(PRIORITY.HIGH);
		ticket.setVersion(1L);

		when(ticketClient.getTicketById("T1")).thenReturn(ticket);
		when(slaRuleRepo.findByPriority(PRIORITY.HIGH)).thenReturn(Mono.empty());

		StepVerifier.create(service.manualAssign("T1", "A1", PRIORITY.HIGH, 1L))
				.expectErrorMatches(e -> e.getMessage().contains("No SLA rule")).verify();
	}

	@Test
	void completeAssignment_success() {
		assignment.setStatus(ASSIGNMENT_STATUS.ASSIGNED);

		when(assignmentRepo.findTopByTicketIdOrderByAssignedAtDesc("T1")).thenReturn(Mono.just(assignment));
		when(assignmentRepo.save(any())).thenReturn(Mono.just(assignment));

		StepVerifier.create(service.completeAssignment("T1"))
				.expectNextMatches(a -> a.getStatus() == ASSIGNMENT_STATUS.COMPLETED).verifyComplete();

		verify(userClient).decrementAssignments("A1");
		verify(ticketClient).resolveTicket("T1");
	}

	@Test
	void completeAssignment_notFound() {
		when(assignmentRepo.findTopByTicketIdOrderByAssignedAtDesc("T1")).thenReturn(Mono.empty());

		StepVerifier.create(service.completeAssignment("T1"))
				.expectErrorMatches(e -> e.getMessage().contains("Assignment not found")).verify();
	}

	@Test
	void completeAssignment_alreadyCompleted() {
		assignment.setStatus(ASSIGNMENT_STATUS.COMPLETED);

		when(assignmentRepo.findTopByTicketIdOrderByAssignedAtDesc("T1")).thenReturn(Mono.just(assignment));

		StepVerifier.create(service.completeAssignment("T1"))
				.expectErrorMatches(e -> e.getMessage().contains("already completed")).verify();
	}

	@Test
	void completeAssignment_escalated() {
		assignment.setStatus(ASSIGNMENT_STATUS.ESCALATED);

		when(assignmentRepo.findTopByTicketIdOrderByAssignedAtDesc("T1")).thenReturn(Mono.just(assignment));

		StepVerifier.create(service.completeAssignment("T1"))
				.expectErrorMatches(e -> e.getMessage().contains("cannot be completed")).verify();
	}

	@Test
	void checkAndEscalate_escalates() {
		assignment.setDueAt(Instant.now().minus(Duration.ofMinutes(10)));
		assignment.setEscalationLevel(0);

		when(assignmentRepo.findTopByTicketIdOrderByAssignedAtDesc("T1")).thenReturn(Mono.just(assignment));
		when(assignmentRepo.save(any())).thenReturn(Mono.just(assignment));

		StepVerifier.create(service.checkAndEscalate("T1"))
				.expectNextMatches(a -> a.getStatus() == ASSIGNMENT_STATUS.ESCALATED && a.isBreached())
				.verifyComplete();

		verify(eventProducer).publishEscalationEvent("T1", "A1", 1);
		verify(ticketClient).logActivity(eq("T1"), eq("A1"), contains("ESCALATED"));
	}

	@Test
	void checkAndEscalate_notFound() {
		when(assignmentRepo.findTopByTicketIdOrderByAssignedAtDesc("T1")).thenReturn(Mono.empty());

		StepVerifier.create(service.checkAndEscalate("T1"))
				.expectErrorMatches(e -> e.getMessage().contains("Assignment not found")).verify();
	}

	@Test
	void checkAndEscalate_notOverdue() {
		assignment.setDueAt(Instant.now().plus(Duration.ofMinutes(10))); // still within SLA
		assignment.setEscalationLevel(0);

		when(assignmentRepo.findTopByTicketIdOrderByAssignedAtDesc("T1")).thenReturn(Mono.just(assignment));

		StepVerifier.create(service.checkAndEscalate("T1"))
				.expectNextMatches(a -> a.getStatus() == ASSIGNMENT_STATUS.ASSIGNED && !a.isBreached())
				.verifyComplete();

		verify(assignmentRepo, never()).save(any());
		verify(eventProducer, never()).publishEscalationEvent(any(), any(), anyInt());
	}

	@Test
	void reassign_success() {
		assignment.setStatus(ASSIGNMENT_STATUS.ASSIGNED);
		assignment.setAgentId("A1");

		when(assignmentRepo.findTopByTicketIdOrderByAssignedAtDesc("T1")).thenReturn(Mono.just(assignment));
		when(assignmentRepo.save(any())).thenReturn(Mono.just(assignment));

		StepVerifier.create(service.reassign("T1", "A2"))
				.expectNextMatches(a -> a.getAgentId().equals("A2") && a.getStatus() == ASSIGNMENT_STATUS.REASSIGNED)
				.verifyComplete();

		verify(eventProducer).publishAssignmentEvent("T1", "A2", "REASSIGNED");
		verify(userClient).decrementAssignments("A1");
		verify(userClient).incrementAssignments("A2");
		verify(ticketClient).assignTicket("T1", "A2");
	}

	@Test
	void reassign_notFound() {
		when(assignmentRepo.findTopByTicketIdOrderByAssignedAtDesc("T1")).thenReturn(Mono.empty());
		StepVerifier.create(service.reassign("T1", "A2"))
				.expectErrorMatches(e -> e.getMessage().contains("Assignment not found")).verify();
	}

	@Test
	void reassign_completedAssignment() {
		assignment.setStatus(ASSIGNMENT_STATUS.COMPLETED);
		assignment.setAgentId("A1");
		when(assignmentRepo.findTopByTicketIdOrderByAssignedAtDesc("T1")).thenReturn(Mono.just(assignment));
		StepVerifier.create(service.reassign("T1", "A2"))
				.expectErrorMatches(e -> e.getMessage().contains("Cannot reassign a completed assignment")).verify();
		verify(assignmentRepo, never()).save(any());
		verify(eventProducer, never()).publishAssignmentEvent(any(), any(), any());
	}

	@Test
	void autoAssign_success() {
		TicketDto ticket = new TicketDto();
		ticket.setId("T1");
		ticket.setStatus(STATUS.OPEN);
		ticket.setPriority(PRIORITY.HIGH);
		ticket.setVersion(1L);
		SlaRule rule = new SlaRule();
		rule.setPriority(PRIORITY.HIGH);
		rule.setResolutionMinutes(30);
		when(ticketClient.getTicketById("T1")).thenReturn(ticket);
		when(slaRuleRepo.findByPriority(PRIORITY.HIGH)).thenReturn(Mono.just(rule));
		when(ruleEngine.pickAgentForTicket(ticket)).thenReturn("A1");
		when(assignmentRepo.save(any())).thenReturn(Mono.just(assignment));
		StepVerifier.create(service.autoAssign("T1"))
				.expectNextMatches(a -> a.getAgentId().equals("A1") && a.getStatus() == ASSIGNMENT_STATUS.ASSIGNED)
				.verifyComplete();
		verify(eventProducer).publishAssignmentEvent("T1", "A1", "AUTO");
		verify(userClient).incrementAssignments("A1");
		verify(ticketClient).assignTicket("T1", "A1");
	}

	@Test
	void autoAssign_ticketNotFound() {
		when(ticketClient.getTicketById("T1")).thenThrow(new RuntimeException("Ticket not found: T1"));
		StepVerifier.create(service.autoAssign("T1"))
				.expectErrorMatches(e -> e.getMessage().contains("Ticket not found")).verify();
	}

	@Test
	void autoAssign_ticketClosed() {
		TicketDto ticket = new TicketDto();
		ticket.setId("T1");
		ticket.setStatus(STATUS.CLOSED);
		ticket.setPriority(PRIORITY.HIGH);
		ticket.setVersion(1L);
		when(ticketClient.getTicketById("T1")).thenReturn(ticket);
		StepVerifier.create(service.autoAssign("T1")).expectErrorMatches(e -> e.getMessage().contains("already closed"))
				.verify();
	}

	@Test
	void autoAssign_ticketAlreadyAssigned() {
		TicketDto ticket = new TicketDto();
		ticket.setId("T1");
		ticket.setStatus(STATUS.ASSIGNED);
		ticket.setPriority(PRIORITY.HIGH);
		ticket.setVersion(1L);
		when(ticketClient.getTicketById("T1")).thenReturn(ticket);
		StepVerifier.create(service.autoAssign("T1"))
				.expectErrorMatches(e -> e.getMessage().contains("already assigned")).verify();
	}

	@Test
	void autoAssign_noSlaRule() {
		TicketDto ticket = new TicketDto();
		ticket.setId("T1");
		ticket.setStatus(STATUS.OPEN);
		ticket.setPriority(PRIORITY.HIGH);
		ticket.setVersion(1L);
		when(ticketClient.getTicketById("T1")).thenReturn(ticket);
		when(slaRuleRepo.findByPriority(PRIORITY.HIGH)).thenReturn(Mono.empty());
		StepVerifier.create(service.autoAssign("T1")).expectErrorMatches(e -> e.getMessage().contains("No SLA rule"))
				.verify();
	}

	@Test
	void autoAssign_noAgentAvailable() {
		TicketDto ticket = new TicketDto();
		ticket.setId("T1");
		ticket.setStatus(STATUS.OPEN);
		ticket.setPriority(PRIORITY.HIGH);
		ticket.setVersion(1L);
		SlaRule rule = new SlaRule();
		rule.setPriority(PRIORITY.HIGH);
		rule.setResolutionMinutes(30);
		when(ticketClient.getTicketById("T1")).thenReturn(ticket);
		when(slaRuleRepo.findByPriority(PRIORITY.HIGH)).thenReturn(Mono.just(rule));
		when(ruleEngine.pickAgentForTicket(ticket)).thenReturn(null);
		StepVerifier.create(service.autoAssign("T1")).expectErrorMatches(e -> e.getMessage().contains("No valid agent"))
				.verify();
	}

	@Test
	void assignmentsPerAgent_success() {
		Assignment a1 = Assignment.builder().ticketId("T1").agentId("A1").status(ASSIGNMENT_STATUS.ASSIGNED)
				.assignedAt(Instant.now().minusSeconds(300)).build();
		Assignment a2 = Assignment.builder().ticketId("T2").agentId("A1").status(ASSIGNMENT_STATUS.COMPLETED)
				.assignedAt(Instant.now().minusSeconds(600)).unassignedAt(Instant.now()).escalationLevel(1).build();

		when(assignmentRepo.findAll()).thenReturn(Flux.just(a1, a2));

		StepVerifier.create(service.assignmentsPerAgent()).expectNextMatches(list -> {
			AgentSummaryDto dto = list.get(0);
			return dto.getAgentId().equals("A1") && dto.getAssignedCount() == 1 && dto.getResolvedCount() == 1
					&& dto.getEscalationLevel() == 0 && dto.getAverageResolutionTimeMinutes() > 0;
		}).verifyComplete();
	}

	@Test
	void assignmentsPerAgent_emptyRepo() {
		when(assignmentRepo.findAll()).thenReturn(Flux.empty());
		StepVerifier.create(service.assignmentsPerAgent()).expectNextMatches(list -> list.isEmpty()).verifyComplete();
	}

	@Test
	void getEscalationSummary_success() {
		EscalationSummaryDto dto = EscalationSummaryDto.builder().level(1).count(2L).build();
		when(assignmentRepo.getEscalationSummary()).thenReturn(Flux.just(dto));
		StepVerifier.create(service.getEscalationSummary())
				.expectNextMatches(
						list -> list.size() == 1 && list.get(0).getLevel() == 1 && list.get(0).getCount() == 2L)
				.verifyComplete();
	}

	@Test
	void getEscalationSummary_empty() {
		when(assignmentRepo.getEscalationSummary()).thenReturn(Flux.empty());
		StepVerifier.create(service.getEscalationSummary()).expectNextMatches(List::isEmpty).verifyComplete();
	}

}
