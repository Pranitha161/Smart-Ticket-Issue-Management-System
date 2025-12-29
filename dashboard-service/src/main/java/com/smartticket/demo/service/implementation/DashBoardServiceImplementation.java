package com.smartticket.demo.service.implementation;

import org.springframework.stereotype.Service;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.CategorySummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.feign.AssignmentClient;
import com.smartticket.demo.feign.TicketClient;
import com.smartticket.demo.service.DashBoardService;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
public class DashBoardServiceImplementation implements DashBoardService {

	private final TicketClient ticketClient;
	private final AssignmentClient assignmentClient;

	DashBoardServiceImplementation(TicketClient ticketClient, AssignmentClient assignmentClient) {
		this.ticketClient = ticketClient;
		this.assignmentClient = assignmentClient;
	}

	@Override
	public Flux<StatusSummaryDto> getTicketStatusSummary() {
		return Flux.defer(() -> Flux.fromIterable(ticketClient.getTicketStatusSummary()))
				.subscribeOn(Schedulers.boundedElastic());
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
	public Flux<AgentSummaryDto> getAssignmentsPerAgent() {
		return Flux.defer(() -> Flux.fromIterable(assignmentClient.getAssignmentsPerAgent()))
				.subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Flux<EscalationSummaryDto> getEscalationSummary() {
		return Flux.defer(() -> Flux.fromIterable(assignmentClient.getEscalationSummary()))
				.subscribeOn(Schedulers.boundedElastic());
	}

}
