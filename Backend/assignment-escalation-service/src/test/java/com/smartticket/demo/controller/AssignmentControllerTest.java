package com.smartticket.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;
import com.smartticket.demo.entity.Assignment;
import com.smartticket.demo.enums.ASSIGNMENT_STATUS;
import com.smartticket.demo.enums.ASSIGNMENT_TYPE;
import com.smartticket.demo.enums.PRIORITY;
import com.smartticket.demo.service.implementation.AssignmentServiceImplementation;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class AssignmentControllerTest {
	@Mock
	private AssignmentServiceImplementation assignmentService;
	@InjectMocks
	private AssignmentController controller;

	@Test
	void manualAssign_success() {
		Assignment assignment = Assignment.builder().id("A1").ticketId("T1").agentId("Agent1").assignedAt(Instant.now())
				.status(ASSIGNMENT_STATUS.ASSIGNED).type(ASSIGNMENT_TYPE.MANUAL).build();
		when(assignmentService.manualAssign("T1", "Agent1", PRIORITY.HIGH, 1L)).thenReturn(Mono.just(assignment));
		StepVerifier.create(controller.manualAssign("T1", "Agent1", PRIORITY.HIGH, 1L)).assertNext(resp -> {
			assertEquals(HttpStatus.OK, resp.getStatusCode());
			assertTrue(resp.getBody().isSuccess());
			assertTrue(resp.getBody().getMessage().contains("Ticket T1 assigned to agent Agent1"));
		}).verifyComplete();
	}

	@Test
	void manualAssign_conflict() {
		when(assignmentService.manualAssign("T1", "Agent1", PRIORITY.HIGH, 1L))
				.thenReturn(Mono.error(new OptimisticLockingFailureException("Conflict")));
		StepVerifier.create(controller.manualAssign("T1", "Agent1", PRIORITY.HIGH, 1L)).assertNext(resp -> {
			assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
			assertFalse(resp.getBody().isSuccess());
			assertTrue(resp.getBody().getMessage().contains("already updated"));
		}).verifyComplete();
	}

	@Test
	void completeAssignment_success() {
		Assignment assignment = Assignment.builder().id("A1").ticketId("T1").agentId("Agent1").assignedAt(Instant.now())
				.status(ASSIGNMENT_STATUS.COMPLETED).type(ASSIGNMENT_TYPE.MANUAL).build();
		when(assignmentService.completeAssignment("T1")).thenReturn(Mono.just(assignment));
		StepVerifier.create(controller.completeAssignment("T1")).assertNext(resp -> {
			assertEquals(HttpStatus.OK, resp.getStatusCode());
			assertTrue(resp.getBody().isSuccess());
			assertTrue(resp.getBody().getMessage().contains("marked as completed"));
		}).verifyComplete();
	}

	@Test
	void checkEscalation_success() {
		Assignment assignment = Assignment.builder().id("A1").ticketId("T1").agentId("Agent1").assignedAt(Instant.now())
				.status(ASSIGNMENT_STATUS.ASSIGNED).type(ASSIGNMENT_TYPE.AUTO).build();
		when(assignmentService.checkAndEscalate("T1")).thenReturn(Mono.just(assignment));
		StepVerifier.create(controller.checkEscalation("T1")).assertNext(resp -> {
			assertEquals(HttpStatus.OK, resp.getStatusCode());
			assertEquals("A1", resp.getBody().getId());
		}).verifyComplete();
	}

	@Test
	void autoAssign_success() {
		Assignment assignment = Assignment.builder().id("A1").ticketId("T1").agentId("Agent1").assignedAt(Instant.now())
				.status(ASSIGNMENT_STATUS.ASSIGNED).type(ASSIGNMENT_TYPE.AUTO).build();
		when(assignmentService.autoAssign("T1")).thenReturn(Mono.just(assignment));
		StepVerifier.create(controller.autoAssign("T1")).assertNext(resp -> {
			assertEquals(HttpStatus.OK, resp.getStatusCode());
			assertEquals("A1", resp.getBody().getId());
		}).verifyComplete();
	}

	@Test
	void getAgentWorkloadSummary_success() {
		AgentSummaryDto dto = AgentSummaryDto.builder().agentId("Agent1").assignedCount(2).resolvedCount(1)
				.averageResolutionTimeMinutes(15.0).build();
		when(assignmentService.assignmentsPerAgent()).thenReturn(Mono.just(List.of(dto)));
		StepVerifier.create(controller.getAgentWorkloadSummary()).assertNext(resp -> {
			assertEquals(HttpStatus.OK, resp.getStatusCode());
			assertEquals(1, resp.getBody().size());
			assertEquals("Agent1", resp.getBody().get(0).getAgentId());
		}).verifyComplete();
	}

	@Test
	void getEscalationSummary_success() {
		EscalationSummaryDto dto = EscalationSummaryDto.builder().level(1).count(3L).build();
		when(assignmentService.getEscalationSummary()).thenReturn(Mono.just(List.of(dto)));
		StepVerifier.create(controller.getEscalationSummary()).assertNext(resp -> {
			assertEquals(HttpStatus.OK, resp.getStatusCode());
			assertEquals(1, resp.getBody().size());
			assertEquals(1, resp.getBody().get(0).getLevel());
			assertEquals(3L, resp.getBody().get(0).getCount());
		}).verifyComplete();
	}

}
