package com.smartticket.demo.service.implementation;

import org.springframework.stereotype.Service;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.CategorySummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.dto.UserStatsDto;
import com.smartticket.demo.dto.UserTicketStatsDto;
import com.smartticket.demo.feign.AssignmentClient;
import com.smartticket.demo.feign.TicketClient;
import com.smartticket.demo.feign.UserClient;
import com.smartticket.demo.service.DashBoardService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class DashBoardServiceImplementation implements DashBoardService {

	private final UserClient userClient;
	private final TicketClient ticketClient;
	private final AssignmentClient assignmentClient;

	DashBoardServiceImplementation(TicketClient ticketClient, AssignmentClient assignmentClient,
			UserClient userClient) {
		this.ticketClient = ticketClient;
		this.assignmentClient = assignmentClient;
		this.userClient = userClient;
	}

	@Override
	@CircuitBreaker(name = "dashboardServiceCircuitBreaker", fallbackMethod = "statusSummaryFallback")
	public Flux<StatusSummaryDto> getTicketStatusSummary() {
		return Flux.defer(() -> Flux.fromIterable(ticketClient.getTicketStatusSummary()))
				.subscribeOn(Schedulers.boundedElastic());
	}

	public Flux<StatusSummaryDto> statusSummaryFallback(Throwable t) {
		System.err.println("Fallback triggered: " + t.getMessage());
		return Flux.empty();
	}

	@Override
	public Flux<PrioritySummaryDto> getTicketStatusPrioritySummary() {
		return Flux.defer(() -> Flux.fromIterable(ticketClient.getTicketStatusPrioritySummary()))
				.subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Flux<CategorySummaryDto> getCategorySummary() {
		return Flux.defer(() -> Flux.fromIterable(ticketClient.getCategorySummary()))
				.subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Mono<UserTicketStatsDto> getUserStats(String userId) {
		return Mono.fromCallable(() -> ticketClient.getUserStats(userId)).subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Mono<UserTicketStatsDto> getAgentStats(String agentId) {
		return Mono.fromCallable(() -> ticketClient.getAgentStats(agentId)).subscribeOn(Schedulers.boundedElastic());

	}

	@Override
	public Mono<UserTicketStatsDto> getGlobalStats() {
		return Mono.fromCallable(() -> ticketClient.getGlobalStats()).subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Mono<UserStatsDto> getStats() {
		return Mono.fromCallable(() -> userClient.getUserStats()).subscribeOn(Schedulers.boundedElastic());

	}

	@Override
	public Flux<AgentSummaryDto> getAssignmentsPerAgent() {
		return Flux.defer(() -> Flux.fromIterable(assignmentClient.getAgentWorkloadSummary()))
				.subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Flux<EscalationSummaryDto> getEscalationSummary() {
		return Flux.defer(() -> Flux.fromIterable(assignmentClient.getEscalationSummary()))
				.subscribeOn(Schedulers.boundedElastic());
	}

}
