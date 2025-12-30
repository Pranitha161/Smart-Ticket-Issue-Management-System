package com.smartticket.demo.service.implementation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.smartticket.demo.dto.CategorySummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.entity.Ticket;
import com.smartticket.demo.entity.TicketActivity;
import com.smartticket.demo.enums.ACTION_TYPE;
import com.smartticket.demo.enums.STATUS;
import com.smartticket.demo.repository.TicketActivityRepository;
import com.smartticket.demo.repository.TicketRepository;
import com.smartticket.demo.service.TicketService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TicketServiceImplementation implements TicketService {

	private final TicketRepository ticketRepo;
	private final TicketActivityRepository ticketActivityRepo;

	TicketServiceImplementation(TicketRepository ticketRepo, TicketActivityRepository ticketActivityRepo) {
		this.ticketRepo = ticketRepo;
		this.ticketActivityRepo = ticketActivityRepo;
	}

	@Override
	public Mono<Ticket> createTicket(Ticket ticket) {
		ticket.setStatus(STATUS.OPEN);
		ticket.setCreatedAt(LocalDateTime.now());
		ticket.setUpdatedAt(LocalDateTime.now());
		return ticketRepo.save(ticket).map(saved -> {
			saved.setDisplayId("TCK-" + saved.getId().substring(0, 6).toUpperCase());
			return saved;
		}).flatMap(ticketRepo::save);
	}

	@Override
	public Mono<Ticket> getTicketById(String id) {
		return ticketRepo.findById(id);
	}

	@Override
	public Flux<Ticket> getAllTickets() {
		return ticketRepo.findAll();
	}

	@Override
	public Mono<Ticket> updateTicketById(String id, Ticket updatedTicket) {
		return ticketRepo.findById(id).flatMap(existing -> {
			existing.setTitle(updatedTicket.getTitle());
			existing.setDescription(updatedTicket.getDescription());
			existing.setPriority(updatedTicket.getPriority());
			existing.setUpdatedAt(LocalDateTime.now());
			return ticketRepo.save(existing);
		});
	}

	@Override
	public Mono<Ticket> closeTicket(String id) {
		return ticketRepo.findById(id).switchIfEmpty(Mono.error(new RuntimeException("Ticket not found")))
				.flatMap(ticket -> {
					if (ticket.getStatus() == STATUS.CLOSED) {
						return Mono.error(new RuntimeException("Ticket already closed"));
					}
					if (ticket.getStatus() == STATUS.OPEN) {
						return Mono.error(new RuntimeException("Ticket must be assigned before closing"));
					}
					ticket.setStatus(STATUS.CLOSED);
					ticket.setUpdatedAt(LocalDateTime.now());
					return ticketRepo.save(ticket).flatMap(saved -> logActivity(saved, ACTION_TYPE.RESOLVED));
				});
	}

	@Override
	public Mono<Ticket> reopenTicket(String id) {
		return ticketRepo.findById(id).flatMap(ticket -> {
			if (ticket.getStatus() != STATUS.RESOLVED && ticket.getStatus() != STATUS.CLOSED) {
				return Mono.error(new IllegalArgumentException("Only RESOLVED or CLOSED tickets can be reopened"));
			}
			ticket.setStatus(STATUS.OPEN);
			ticket.setUpdatedAt(LocalDateTime.now());
			return ticketRepo.save(ticket);
		});
	}

	@Override
	public Mono<Void> deleteTicket(String id) {
		return ticketRepo.deleteById(id);
	}

	@Override
	public Mono<Ticket> resolveTicket(String id) {
		return ticketRepo.findById(id).switchIfEmpty(Mono.error(new RuntimeException("Ticket not found")))
				.flatMap(ticket -> {
					if (ticket.getStatus() == STATUS.CLOSED) {
						return Mono.error(new RuntimeException("Ticket already closed"));
					}
					if (ticket.getStatus() == STATUS.RESOLVED) {
						return Mono.error(new RuntimeException("Ticket already resolved"));
					}
					if (ticket.getStatus() == STATUS.OPEN) {
						return Mono.error(new RuntimeException("Ticket must be assigned before resolving"));
					}
					ticket.setStatus(STATUS.RESOLVED);
					ticket.setUpdatedAt(LocalDateTime.now());
					return ticketRepo.save(ticket).flatMap(saved -> logActivity(saved, ACTION_TYPE.RESOLVED));
				});
	}

	private Mono<Ticket> logActivity(Ticket ticket, ACTION_TYPE action) {
		TicketActivity activity = TicketActivity.builder().ticketId(ticket.getId()).actionType(action)
				.timestamp(Instant.now()).details("Ticket " + action).build();
		return ticketActivityRepo.save(activity).thenReturn(ticket);
	}

	@Override
	public Flux<Ticket> getTicketsByUserId(String userId) {
		return ticketRepo.findByCreatedBy(userId);
	}

	@Override
	public Mono<List<StatusSummaryDto>> statusSummary() {
		return ticketRepo.getStatusSummary().collectList();
	}

	@Override
	public Mono<List<PrioritySummaryDto>> prioritySummary() {
		return ticketRepo.getPrioritySummary().collectList();
	}

	public Mono<List<CategorySummaryDto>> getCategorySummary() {
		return ticketRepo.getCategorySummary().collectList();
	}

}
