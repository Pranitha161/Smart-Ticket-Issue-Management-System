package com.smartticket.demo.service.implementation;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.Ticket;
import com.smartticket.demo.enums.STATUS;
import com.smartticket.demo.repository.TicketRepository;
import com.smartticket.demo.service.TicketService;

import reactor.core.publisher.Mono;

@Service
public class TicketServiceImplementation implements TicketService {

	private final TicketRepository ticketRepo;

	TicketServiceImplementation(TicketRepository ticketRepo) {
		this.ticketRepo = ticketRepo;
	}

	@Override
	public Mono<ResponseEntity<ApiResponse>> createTicket(Ticket ticket) {
		ticket.setStatus(STATUS.OPEN);
		ticket.setCreatedAt(LocalDateTime.now());
		ticket.setUpdatedAt(LocalDateTime.now());
		return ticketRepo.save(ticket).map(
				savedTicket -> ResponseEntity.ok(new ApiResponse(true, "Created ticked " + savedTicket.getTicketId())));
	}

	@Override
	public Mono<ResponseEntity<Ticket>> getTicketById(String id) {
		return ticketRepo.findById(id).map(Ticket -> ResponseEntity.ok(Ticket))
				.switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

	@Override
	public Mono<ResponseEntity<ApiResponse>> updateTicketById(String id, Ticket updatedTicket) {
		return ticketRepo.findById(id).flatMap(ticket -> {
			ticket.setTitle(updatedTicket.getTitle());
			ticket.setDescription(updatedTicket.getDescription());
			ticket.setPriority(updatedTicket.getPriority());
			ticket.setUpdatedAt(LocalDateTime.now());
			return ticketRepo.save(ticket);
		}).map(saved -> ResponseEntity.ok(new ApiResponse(true, "Ticket updated successfully " + saved.getTicketId())))
				.switchIfEmpty(Mono.just(
						ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Ticket not found"))));
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
	public Mono<ResponseEntity<ApiResponse>> closeTicket(String id) {
		return ticketRepo.findById(id).flatMap(ticket -> {
			ticket.setStatus(STATUS.CLOSED);
			ticket.setUpdatedAt(LocalDateTime.now());
			return ticketRepo.save(ticket);
		}).map(saved -> ResponseEntity.ok(new ApiResponse(true, "Ticket closed successfully"))).switchIfEmpty(Mono
				.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Ticket not found"))));
	}

	@Override
	public Mono<ResponseEntity<ApiResponse>> reopenTicket(String id) {
		return ticketRepo.findById(id).flatMap(ticket -> {
			if (ticket.getStatus() != STATUS.RESOLVED && ticket.getStatus() != STATUS.CLOSED) {
				return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new ApiResponse(false, "Only RESOLVED or CLOSED tickets can be reopened")));
			}
			ticket.setStatus(STATUS.OPEN);
			ticket.setUpdatedAt(LocalDateTime.now());
			return ticketRepo.save(ticket)
					.map(saved -> ResponseEntity.ok(new ApiResponse(true, "Ticket reopened successfully")));
		}).switchIfEmpty(Mono
				.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Ticket not found"))));
	}

	@Override
	public Mono<ResponseEntity<ApiResponse>> deleteTicket(String id) {
		return ticketRepo.findById(id)
				.flatMap(ticket -> ticketRepo.delete(ticket)
						.then(Mono.just(ResponseEntity.ok(new ApiResponse(true, "Ticket deleted successfully")))))
				.switchIfEmpty(Mono.just(
						ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Ticket not found"))));
	}

	@Override
	public Mono<ResponseEntity<ApiResponse>> resolveTicket(String id) {
		return ticketRepo.findById(id).flatMap(ticket -> {
			ticket.setStatus(STATUS.RESOLVED);
			ticket.setUpdatedAt(LocalDateTime.now());
			return ticketRepo.save(ticket);
		}).map(saved -> ResponseEntity.ok(new ApiResponse(true, "Ticket closed successfully"))).switchIfEmpty(Mono
				.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Ticket not found"))));
	}

}
