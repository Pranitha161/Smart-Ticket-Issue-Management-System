package com.smartticket.demo.repository;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.smartticket.demo.entity.Assignment;
import com.smartticket.demo.enums.ASSIGNMENT_STATUS;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AssignmentRepository extends ReactiveMongoRepository<Assignment, String> {

	Flux<Assignment> findByBreachedFalseAndDueAtBefore(LocalDateTime now);

	Flux<Assignment> findByAgentId(String agentId);

	Flux<Assignment> findByTicketId(String ticketId);

	Flux<Assignment> findByStatus(ASSIGNMENT_STATUS status);

	Mono<Assignment> findTopByTicketIdOrderByAssignedAtDesc(String ticketId);
}
