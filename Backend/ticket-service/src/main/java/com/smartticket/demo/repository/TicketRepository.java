package com.smartticket.demo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.smartticket.demo.entity.Ticket;

import reactor.core.publisher.Mono;

@Repository
public interface TicketRepository extends ReactiveMongoRepository<Ticket, String> {

	Mono<Ticket> findCreatedBy(String userId);
	
	Mono<Ticket> findByCategoryId(String categoryId);
	
}
