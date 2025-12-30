package com.smartticket.demo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.smartticket.demo.entity.TicketActivity;

import reactor.core.publisher.Flux;

public interface TicketActivityRepository extends ReactiveMongoRepository<TicketActivity, String> {
	Flux<TicketActivity> findByTicketIdOrderByTimestampAsc(String ticketId);

	Flux<TicketActivity> findByTicketId(String ticketId);
}
