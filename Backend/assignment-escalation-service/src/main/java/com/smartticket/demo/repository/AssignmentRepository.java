package com.smartticket.demo.repository;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.smartticket.demo.entity.Assignment;

import reactor.core.publisher.Flux;

@Repository
public interface AssignmentRepository extends ReactiveMongoRepository<Assignment, String> {

	Flux<Assignment> findByBreachedFalseAndDueAtBefore(LocalDateTime now);

	Flux<Assignment> findByAgentId(String agentId);

	Flux<Assignment> findByTicketId(String ticketId);
}
