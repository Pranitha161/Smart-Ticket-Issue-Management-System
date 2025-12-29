package com.smartticket.demo.service.implementation;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.feign.AssignmentClient;
import com.smartticket.demo.feign.TicketClient;
import com.smartticket.demo.service.DashBoardService;

import reactor.core.publisher.Flux;

public class DashBoardServiceImplementation implements DashBoardService {

	private final TicketClient ticketClient;
	private final AssignmentClient assignmentClient;

	DashBoardServiceImplementation(TicketClient ticketClient, AssignmentClient assignmentClient) {
		this.ticketClient = ticketClient;
		this.assignmentClient = assignmentClient;
	}

	@Override
	public Flux<StatusSummaryDto> getTicketStatusSummary() {
		return Flux.defer(() -> Flux.fromIterable(ticketClient.getTicketStatusSummary()));
	}

	@Override
	public Flux<PrioritySummaryDto> getTicketStatusPrioritySummary() {
		return Flux.defer(() -> Flux.fromIterable(ticketClient.getTicketStatusPrioritySummary()));
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
