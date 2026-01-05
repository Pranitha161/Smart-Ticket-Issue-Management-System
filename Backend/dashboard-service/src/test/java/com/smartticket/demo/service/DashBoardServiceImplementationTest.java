package com.smartticket.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartticket.demo.dto.AgentStatsDto;
import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.CategorySummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.dto.UserStatsDto;
import com.smartticket.demo.dto.UserTicketStatsDto;
import com.smartticket.demo.enums.PRIORITY;
import com.smartticket.demo.enums.STATUS;
import com.smartticket.demo.feign.AssignmentClient;
import com.smartticket.demo.feign.TicketClient;
import com.smartticket.demo.feign.UserClient;
import com.smartticket.demo.service.implementation.DashBoardServiceImplementation;

import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class DashBoardServiceImplementationTest {
	@Mock
	private TicketClient ticketClient;
	@Mock
	private UserClient userClient;
	@Mock
	private AssignmentClient assignmentClient;
	@InjectMocks
	private DashBoardServiceImplementation service;

	@Test
	void getTicketStatusSummary_success() {
		StatusSummaryDto s1 = new StatusSummaryDto(STATUS.OPEN, 2L);
		when(ticketClient.getTicketStatusSummary()).thenReturn(List.of(s1));
		StepVerifier.create(service.getTicketStatusSummary()).expectNext(s1).verifyComplete();
	}

	@Test
	void getTicketStatusSummary_fallback() {
		when(ticketClient.getTicketStatusSummary()).thenThrow(new RuntimeException("Service down"));

		StepVerifier.create(service.getTicketStatusSummary()).verifyError(RuntimeException.class);
	}

	@Test
	void getTicketStatusPrioritySummary_success() {
		PrioritySummaryDto p1 = new PrioritySummaryDto(PRIORITY.HIGH, 3L);
		when(ticketClient.getTicketStatusPrioritySummary()).thenReturn(List.of(p1));
		StepVerifier.create(service.getTicketStatusPrioritySummary()).expectNext(p1).verifyComplete();
	}

	@Test
	void getCategorySummary_success() {
		CategorySummaryDto c1 = new CategorySummaryDto("C1", 5L);
		when(ticketClient.getCategorySummary()).thenReturn(List.of(c1));
		StepVerifier.create(service.getCategorySummary()).expectNext(c1).verifyComplete();
	}

	@Test
	void getUserStats_success() {
		UserTicketStatsDto stats = new UserTicketStatsDto(10L, 3L, 5L, 2L);
		when(ticketClient.getUserStats("U1")).thenReturn(stats);
		StepVerifier.create(service.getUserStats("U1")).assertNext(result -> assertEquals(10L, result.getTotal()))
				.verifyComplete();
	}

	@Test
	void getAgentStats_success() {
		UserTicketStatsDto stats = new UserTicketStatsDto(4L, 1L, 2L, 1L);
		when(ticketClient.getAgentStats("A1")).thenReturn(stats);
		StepVerifier.create(service.getAgentStats("A1")).assertNext(result -> assertEquals(4L, result.getTotal()))
				.verifyComplete();
	}

	@Test
	void getGlobalStats_success() {
		UserTicketStatsDto stats = new UserTicketStatsDto(20L, 6L, 10L, 4L);
		when(ticketClient.getGlobalStats()).thenReturn(stats);
		StepVerifier.create(service.getGlobalStats()).assertNext(result -> assertEquals(20L, result.getTotal()))
				.verifyComplete();
	}

	@Test
	void getStats_success() {
		UserStatsDto stats = new UserStatsDto();
		stats.setTotalUsers(100L);
		when(userClient.getUserStats()).thenReturn(stats);
		StepVerifier.create(service.getStats()).assertNext(result -> assertEquals(100L, result.getTotalUsers()))
				.verifyComplete();
	}

	@Test
	void getAgentStatsById_success() {
		AgentStatsDto stats = new AgentStatsDto();
		stats.setAgentId("A1");
		stats.setResolutionRate(5L);
		when(userClient.getAgentStats("A1")).thenReturn(stats);
		StepVerifier.create(service.getAgentStatsById("A1"))
				.assertNext(result -> assertEquals("A1", result.getAgentId())).verifyComplete();
	}

	@Test
	void getEscalationSummary_success() {
		EscalationSummaryDto esc = EscalationSummaryDto.builder().level(1).count(2L).build();
		when(assignmentClient.getEscalationSummary()).thenReturn(List.of(esc));
		StepVerifier.create(service.getEscalationSummary()).expectNext(esc).verifyComplete();
	}

	@Test
	void getAllAgentStats_success() {
		AgentStatsDto stats = AgentStatsDto.builder().agentId("A1").currentAssignments(5).build();
		when(userClient.getAllAgentStats()).thenReturn(List.of(stats));
		StepVerifier.create(service.getAllAgentStats()).expectNext(stats).verifyComplete();
	}

	@Test
	void getCategorySummary_emptyList() {
		when(ticketClient.getCategorySummary()).thenReturn(List.of());
		StepVerifier.create(service.getCategorySummary()).verifyComplete();
	}

	@Test
	void getUserStats_error() {
		when(ticketClient.getUserStats("U1")).thenThrow(new RuntimeException("Service unavailable"));
		StepVerifier.create(service.getUserStats("U1")).verifyError(RuntimeException.class);
	}

	@Test
	void statusSummaryFallback_returnsEmpty() {
		StepVerifier.create(service.statusSummaryFallback(new RuntimeException("Circuit breaker open")))
				.verifyComplete();
	}

	@Test
	void getAllAgentStats_multipleAgents() {
		AgentStatsDto a1 = AgentStatsDto.builder().agentId("A1").currentAssignments(5).build();
		AgentStatsDto a2 = AgentStatsDto.builder().agentId("A2").currentAssignments(3).build();
		when(userClient.getAllAgentStats()).thenReturn(List.of(a1, a2));
		StepVerifier.create(service.getAllAgentStats()).expectNext(a1).expectNext(a2).verifyComplete();
	}

	@Test
	void getGlobalStats_zeroTickets() {
		UserTicketStatsDto stats = new UserTicketStatsDto(0L, 0L, 0L, 0L);
		when(ticketClient.getGlobalStats()).thenReturn(stats);
		StepVerifier.create(service.getGlobalStats()).assertNext(result -> assertEquals(0L, result.getTotal()))
				.verifyComplete();
	}

	@Test
	void getAssignmentsPerAgent_success() {
		AgentSummaryDto summary = AgentSummaryDto.builder().agentId("A1").assignedCount(2L).build();
		when(assignmentClient.getAgentWorkloadSummary()).thenReturn(List.of(summary));
		StepVerifier.create(service.getAssignmentsPerAgent()).expectNext(summary).verifyComplete();
	}

	@Test
	void getAgentStatsById_error() {
		when(userClient.getAgentStats("A1")).thenThrow(new RuntimeException("Agent not found"));
		StepVerifier.create(service.getAgentStatsById("A1")).verifyError(RuntimeException.class);
	}
	
	@Test
	void statusSummaryFallback_returnsEmptyFlux() {
	    StepVerifier.create(service.statusSummaryFallback(new RuntimeException("CB open")))
	        .verifyComplete();
	}

	@Test
	void getTicketStatusPrioritySummary_multiple() {
	    PrioritySummaryDto p1 = new PrioritySummaryDto(PRIORITY.HIGH, 3L);
	    PrioritySummaryDto p2 = new PrioritySummaryDto(PRIORITY.LOW, 1L);
	    when(ticketClient.getTicketStatusPrioritySummary()).thenReturn(List.of(p1, p2));
	    StepVerifier.create(service.getTicketStatusPrioritySummary()).expectNext(p1).expectNext(p2).verifyComplete();
	}

	@Test
	void getCategorySummary_empty() {
	    when(ticketClient.getCategorySummary()).thenReturn(List.of());
	    StepVerifier.create(service.getCategorySummary()).verifyComplete();
	}

	@Test
	void getGlobalStats_extremeValues() {
	    UserTicketStatsDto stats = new UserTicketStatsDto(Long.MAX_VALUE, 0L, Long.MAX_VALUE, 0L);
	    when(ticketClient.getGlobalStats()).thenReturn(stats);
	    StepVerifier.create(service.getGlobalStats())
	        .assertNext(result -> assertEquals(Long.MAX_VALUE, result.getTotal()))
	        .verifyComplete();
	}

	@Test
	void getAllAgentStats_emptyList() {
	    when(userClient.getAllAgentStats()).thenReturn(List.of());
	    StepVerifier.create(service.getAllAgentStats()).verifyComplete();
	}


}
