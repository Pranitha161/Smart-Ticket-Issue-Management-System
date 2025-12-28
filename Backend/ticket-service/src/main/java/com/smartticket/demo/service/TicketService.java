package com.smartticket.demo.service;

import org.springframework.http.ResponseEntity;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.Ticket;

import reactor.core.publisher.Mono;

public interface TicketService {
	
	Mono<ResponseEntity<ApiResponse>> createTicket(Ticket ticket);
	
	Mono<ResponseEntity<Ticket>> getTicketById(String id);
	
	Mono<ResponseEntity<ApiResponse>> updateTicketById(String id,Ticket updateTicket);
	
	Mono<ResponseEntity<ApiResponse>> closeTicket(String id);
	
	Mono<ResponseEntity<ApiResponse>> reopenTicket(String id);
	
	Mono<ResponseEntity<ApiResponse>> resolveTicket(String id);

	Mono<ResponseEntity<ApiResponse>> deleteTicket(String id);
	
	

}
