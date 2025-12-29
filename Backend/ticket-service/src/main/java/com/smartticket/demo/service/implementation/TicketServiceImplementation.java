package com.smartticket.demo.service.implementation;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.smartticket.demo.entity.Ticket;
import com.smartticket.demo.enums.STATUS;
import com.smartticket.demo.repository.TicketRepository;
import com.smartticket.demo.service.TicketService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TicketServiceImplementation implements TicketService {

	private final TicketRepository ticketRepo;

	TicketServiceImplementation(TicketRepository ticketRepo) {
		this.ticketRepo = ticketRepo;
	}

	@Override
	public Mono<Ticket> createTicket(Ticket ticket) {
		ticket.setStatus(STATUS.OPEN);
		ticket.setCreatedAt(LocalDateTime.now());
		ticket.setUpdatedAt(LocalDateTime.now());
		return ticketRepo.save(ticket);
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

//	public Mono<ResponseEntity<ApiResponse>> getTicketsByUser(String userId) {
//		return ticketRepo.findCreatedBy(userId).collectList()
//				.map(list -> ResponseEntity.ok(new ApiResponse(true, "Tickets fetched successfully", list)));
//	}
//	public Mono<ResponseEntity<ApiResponse>> getTicketsByAgent(String agentId) {
//		return ticketRepo.findByAssignedTo(agentId).collectList()
//				.map(list -> ResponseEntity.ok(new ApiResponse(true, "Tickets fetched successfully", list)));
//	}

	@Override
	public Mono<Ticket> closeTicket(String id) {
		return ticketRepo.findById(id).flatMap(ticket -> {
			ticket.setStatus(STATUS.CLOSED);
			ticket.setUpdatedAt(LocalDateTime.now());
			return ticketRepo.save(ticket);
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
		return ticketRepo.findById(id).flatMap(ticket -> {
			ticket.setStatus(STATUS.RESOLVED);
			ticket.setUpdatedAt(LocalDateTime.now());
			return ticketRepo.save(ticket);
		});
	}

}
