package com.smartticket.demo.repository;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.smartticket.demo.dto.CategorySummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.entity.Ticket;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TicketRepository extends ReactiveMongoRepository<Ticket, String> {
	
	Mono<Boolean> existsByCreatedByAndTitle(String createdBy, String title);

	Flux<Ticket> findByCreatedBy(String userId);

	Flux<Ticket> findByCategoryId(String categoryId);

	Flux<Ticket> findTop5ByOrderByCreatedAtDesc();

	Flux<Ticket> findTop5ByCreatedByOrderByCreatedAtDesc(String createdBy);

	Flux<Ticket> findTop5ByAssignedToOrderByCreatedAtDesc(String agentId);

	Flux<Ticket> findByAssignedTo(String agentId);

	@Aggregation(pipeline = { "{ $group: { _id: '$status', count: { $sum: 1 } } }",
			"{ $project: { status: '$_id', count: 1, _id: 0 } }" })
	Flux<StatusSummaryDto> getStatusSummary();

	@Aggregation(pipeline = { "{ $group: { _id: '$priority', count: { $sum: 1 } } }",
			"{ $project: { priority: '$_id', count: 1, _id: 0 } }" })
	Flux<PrioritySummaryDto> getPrioritySummary();

	@Aggregation(pipeline = { "{ $group: { _id: '$categoryId', count: { $sum: 1 } } }",
			"{ $project: { categoryId: '$_id', count: 1, _id: 0 } }" })
	Flux<CategorySummaryDto> getCategorySummary();

	@Aggregation(pipeline = { "{ $match: { createdBy: ?0 } }", "{ $group: { _id: '$status', count: { $sum: 1 } } }",
			"{ $project: { status: '$_id', count: 1, _id: 0 } }" })
	Flux<StatusSummaryDto> getStatusSummaryByUserId(String userId);

	@Aggregation(pipeline = { "{ $match: { createdBy: ?0 } }", "{ $group: { _id: '$priority', count: { $sum: 1 } } }",
			"{ $project: { priority: '$_id', count: 1, _id: 0 } }" })
	Flux<PrioritySummaryDto> getPrioritySummaryByUserId(String userId);

	@Aggregation(pipeline = { "{ $match: { assignedTo: ?0 } }", "{ $group: { _id: '$status', count: { $sum: 1 } } }",
			"{ $project: { status: '$_id', count: 1, _id: 0 } }" })
	Flux<StatusSummaryDto> getStatusSummaryByAgentId(String userId);

	@Aggregation(pipeline = { "{ $match: { assignedTo: ?0 } }", "{ $group: { _id: '$priority', count: { $sum: 1 } } }",
			"{ $project: { priority: '$_id', count: 1, _id: 0 } }" })
	Flux<PrioritySummaryDto> getPrioritySummaryByAgentId(String userId);

}
