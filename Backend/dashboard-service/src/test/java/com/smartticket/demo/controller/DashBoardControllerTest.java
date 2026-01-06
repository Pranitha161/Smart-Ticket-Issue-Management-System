package com.smartticket.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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
import com.smartticket.demo.service.implementation.DashBoardServiceImplementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DashBoardControllerTest {
	@Mock
	private DashBoardServiceImplementation dashboardService;
	@InjectMocks
	private DashBoardController controller;

	@Test
	void getTicketStatusSummary_success() {
		StatusSummaryDto s1 = new StatusSummaryDto(STATUS.OPEN, 2L);
		when(dashboardService.getTicketStatusSummary()).thenReturn(Flux.just(s1));
		StepVerifier.create(controller.getTicketStatusSummary()).expectNext(s1).verifyComplete();
	}

	@Test
	void getTicketStatusPrioritySummary_success() {
		PrioritySummaryDto p1 = new PrioritySummaryDto(PRIORITY.HIGH, 3L);
		when(dashboardService.getTicketStatusPrioritySummary()).thenReturn(Flux.just(p1));
		StepVerifier.create(controller.getTicketStatusPrioritySummary()).expectNext(p1).verifyComplete();
	}

	@Test
	void getUserStats_success() {
		UserTicketStatsDto stats = new UserTicketStatsDto(10L, 3L, 5L, 2L);
		when(dashboardService.getUserStats("U1")).thenReturn(Mono.just(stats));
		StepVerifier.create(controller.getUserStats("U1")).assertNext(result -> assertEquals(10L, result.getTotal()))
				.verifyComplete();
	}

	@Test
	void getCategorySummary_success() {
		CategorySummaryDto c1 = new CategorySummaryDto("C1", 5L);
		when(dashboardService.getCategorySummary()).thenReturn(Flux.just(c1));
		StepVerifier.create(controller.getCategorySummary()).expectNext(c1).verifyComplete();
	}

	@Test
	void getAgentStats_success() {
		UserTicketStatsDto stats = new UserTicketStatsDto(4L, 1L, 2L, 1L);
		when(dashboardService.getAgentStats("A1")).thenReturn(Mono.just(stats));
		StepVerifier.create(controller.getAgentStats("A1")).assertNext(result -> assertEquals(4L, result.getTotal()))
				.verifyComplete();
	}

	@Test
	void getGlobalStats_success() {
		UserTicketStatsDto stats = new UserTicketStatsDto(20L, 6L, 10L, 4L);
		when(dashboardService.getGlobalStats()).thenReturn(Mono.just(stats));
		StepVerifier.create(controller.getGlobalStats()).assertNext(result -> assertEquals(20L, result.getTotal()))
				.verifyComplete();
	}

	@Test
	void getStats_success() {
		UserStatsDto stats = new UserStatsDto();
		stats.setTotalUsers(100L);
		stats.setActiveUsers(20);
		stats.setAdmins(10);
		when(dashboardService.getStats()).thenReturn(Mono.just(stats));
		StepVerifier.create(controller.getStats()).assertNext(result -> assertEquals(100L, result.getTotalUsers()))
				.verifyComplete();
	}

	@Test
	void getAssignmentsPerAgent_success() {
		AgentSummaryDto summary = AgentSummaryDto.builder().agentId("A1").assignedCount(20).build();
		when(dashboardService.getAssignmentsPerAgent()).thenReturn(Flux.just(summary));
		StepVerifier.create(controller.getAssignmentsPerAgent()).expectNext(summary).verifyComplete();
	}

	@Test
	void getEscalationSummary_success() {
		EscalationSummaryDto esc = EscalationSummaryDto.builder().count(10).level(0).build();
		when(dashboardService.getEscalationSummary()).thenReturn(Flux.just(esc));
		StepVerifier.create(controller.getEscalationSummary()).expectNext(esc).verifyComplete();
	}

	@Test
	void getAgentSummaryStats_success() {
		AgentStatsDto stats = new AgentStatsDto();
		stats.setAgentId("A1");
		stats.setCurrentAssignments(10);
		stats.setResolutionRate(2L);
		when(dashboardService.getAgentStatsById("A1")).thenReturn(Mono.just(stats));
		StepVerifier.create(controller.getAgentSummaryStats("A1"))
				.assertNext(result -> assertEquals("A1", result.getAgentId())).verifyComplete();
	}

	@Test
	void getAllAgentStats_success() {
		AgentStatsDto stats = new AgentStatsDto();
		stats.setAgentId("A1");
		stats.setCurrentAssignments(10);
		stats.setResolutionRate(2L);
		when(dashboardService.getAllAgentStats()).thenReturn(Flux.just(stats));
		StepVerifier.create(controller.getAllAgentStats()).expectNext(stats).verifyComplete();
	}
}
