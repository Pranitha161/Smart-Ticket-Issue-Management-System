package com.smartticket.demo.service.implementation;

import java.util.Map;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;
import com.smartticket.demo.feign.AssignmentClient;
import com.smartticket.demo.feign.TicketClient;
import com.smartticket.demo.service.DashBoardService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DashBoardServiceImplementation implements DashBoardService {

	private final TicketClient ticketClient;
	private final AssignmentClient assignmentClient;

	DashBoardServiceImplementation(TicketClient ticketClient,AssignmentClient assignmentClient) {
		this.ticketClient = ticketClient;
		this.assignmentClient=assignmentClient;
	}

	@Override
	public Mono<Map<String, Long>> getTicketStatusSummary() {
		return Mono.fromCallable(ticketClient::getTicketStatusSummary);
	}
	
	@Override
	public Mono<Map<String, Map<String, Long>>> getTicketStatusPrioritySummary() {
		return Mono.fromCallable(ticketClient::getTicketStatusPrioritySummary);
	}

	@Override
	public Flux<AgentSummaryDto> getAssignmentsPerAgent() {
		return Flux.defer(() -> Flux.fromIterable(assignmentClient.getAssignmentsPerAgent()));
	}
	
	@Override
	public Flux<EscalationSummaryDto> getEscalationSummary() {
		return Flux.defer(() -> Flux.fromIterable(assignmentClient.getEscalationSummary()));
	}
	

}
