package com.smartticket.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.Ticket;
import com.smartticket.demo.service.implementation.TicketServiceImplementation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {
	
	@Autowired
	private TicketServiceImplementation ticketService;
	
	@PostMapping("/create")
	public Mono<ResponseEntity<ApiResponse>> createTicket(@Valid @RequestBody Ticket ticket){
		return ticketService.createTicket(ticket);
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Ticket>> getTicket(@PathVariable String id){
		return ticketService.getTicketById(id);
	}
	
	@PutMapping("/{id}/close")
	public  Mono<ResponseEntity<ApiResponse>> closeTicket(@PathVariable String id){
		return ticketService.closeTicket(id);
	}
	
	@PutMapping("/{id}/resolve")
	public  Mono<ResponseEntity<ApiResponse>> resolveTicket(@PathVariable String id){
		return ticketService.resolveTicket(id);
	}
	
	@PutMapping("/{id}/reopen")
	public  Mono<ResponseEntity<ApiResponse>> reopenTicket(@PathVariable String id){
		return ticketService.reopenTicket(id);
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<ApiResponse>> deleteTicket(@PathVariable String id){
		return ticketService.deleteTicket(id);
	}
	
	

}
