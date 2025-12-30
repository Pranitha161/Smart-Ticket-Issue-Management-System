package com.smartticket.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartticket.demo.dto.CategorySummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.Ticket;
import com.smartticket.demo.service.implementation.TicketServiceImplementation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

	@Autowired
	private TicketServiceImplementation ticketService;

	@PostMapping("/create")
	public Mono<ResponseEntity<ApiResponse>> createTicket(@Valid @RequestBody Ticket ticket) {
		return ticketService.createTicket(ticket)
				.map(saved -> ResponseEntity.status(HttpStatus.CREATED)
						.body(new ApiResponse(true, "Ticket created successfully" + saved.getDisplayId())));
	}

	@GetMapping("/user/{userId}")
	public Flux<Ticket> getTicketsByUserId(@PathVariable String userId) {
		return ticketService.getTicketsByUserId(userId);
	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<Ticket>> getTicketById(@PathVariable String id) {
		return ticketService.getTicketById(id).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping
	public Flux<Ticket> getAllTickets() {
		return ticketService.getAllTickets();
	}

	@PutMapping("/{id}")
	public Mono<ResponseEntity<ApiResponse>> updateTicket(@PathVariable String id,
			@Valid @RequestBody Ticket updatedTicket) {
		return ticketService.updateTicketById(id, updatedTicket)
				.map(saved -> ResponseEntity.ok(new ApiResponse(true, "Ticket updated successfully")))
				.defaultIfEmpty(
						ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Ticket not found")));
	}

	@PutMapping("/{id}/close")
	public Mono<ResponseEntity<ApiResponse>> closeTicket(@PathVariable String id) {
		return ticketService.closeTicket(id)
				.map(saved -> ResponseEntity.ok(new ApiResponse(true, "Ticket closed successfully"))).defaultIfEmpty(
						ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Ticket not found")));
	}

	@PutMapping("/{id}/resolve")
	public Mono<ResponseEntity<ApiResponse>> resolveTicket(@PathVariable String id) {
		return ticketService.reopenTicket(id)
				.map(saved -> ResponseEntity.ok(new ApiResponse(true, "Ticket resolved successfully"))).defaultIfEmpty(
						ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Ticket not found")));
	}

	@PutMapping("/{id}/reopen")
	public Mono<ResponseEntity<ApiResponse>> reopenTicket(@PathVariable String id) {
		return ticketService.reopenTicket(id)
				.map(saved -> ResponseEntity.ok(new ApiResponse(true, "Ticket reopened successfully")))	
				.defaultIfEmpty(
						ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Ticket not found")));
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<ApiResponse>> deleteTicket(@PathVariable String id) {
		return ticketService.deleteTicket(id)
				.then(Mono.just(ResponseEntity.ok(new ApiResponse(true, "Ticket deleted successfully"))));
	}

	@GetMapping("/status-summary")
	public Mono<List<StatusSummaryDto>> getTicketStatusSummary() {
		return ticketService.statusSummary();
	}

	@GetMapping("/status-priority-summary")
	public Mono<List<PrioritySummaryDto>> getTicketStatusPrioritySummary() {
		return ticketService.prioritySummary();
	}

	@GetMapping("/category-summary")
	public Mono<List<CategorySummaryDto>> getCategorySummary() {
		return ticketService.getCategorySummary();
	}

}
