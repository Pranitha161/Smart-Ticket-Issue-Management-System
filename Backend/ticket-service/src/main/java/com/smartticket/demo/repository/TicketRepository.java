package com.smartticket.demo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.smartticket.demo.entity.Ticket;

import reactor.core.publisher.Mono;

public interface TicketRepository extends ReactiveMongoRepository<Ticket, String>{
	
	Mono<Ticket> findCreatedBy(String userId);
	
	Mono<Ticket> findByAssignedTo(String agentId);
}
