package com.smartticket.demo.service;

import java.util.List;

import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.entity.Ticket;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TicketService {

	Mono<Ticket> createTicket(Ticket ticket);

	Mono<Ticket> getTicketById(String id);

	Mono<Ticket> updateTicketById(String id, Ticket updatedTicket);

	Mono<Ticket> closeTicket(String id);

	Mono<Ticket> reopenTicket(String id);

	Mono<Ticket> resolveTicket(String id);

	Mono<Void> deleteTicket(String id);

	Flux<Ticket> getAllTickets();

	Flux<Ticket> getTicketsByUserId(String userId);

	Mono<List<StatusSummaryDto>> statusSummary();

	Mono<List<PrioritySummaryDto>> prioritySummary();

}
