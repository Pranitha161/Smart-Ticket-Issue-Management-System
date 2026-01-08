package com.smartticket.demo.service.implementation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartticket.demo.dto.CategorySummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.dto.UserTicketStatsDto;
import com.smartticket.demo.entity.Ticket;
import com.smartticket.demo.entity.TicketActivity;
import com.smartticket.demo.enums.ACTION_TYPE;
import com.smartticket.demo.enums.PRIORITY;
import com.smartticket.demo.enums.STATUS;
import com.smartticket.demo.feign.UserClient;
import com.smartticket.demo.producer.TicketEventProducer;
import com.smartticket.demo.repository.TicketActivityRepository;
import com.smartticket.demo.repository.TicketRepository;
import com.smartticket.demo.service.TicketService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TicketServiceImplementation implements TicketService {

	private final TicketRepository ticketRepo;
	private final TicketActivityRepository ticketActivityRepo;
	private final TicketEventProducer ticketEventProducer;
	private final UserClient userClient;

	TicketServiceImplementation(TicketRepository ticketRepo, TicketActivityRepository ticketActivityRepo,
			TicketEventProducer ticketEventProducer,UserClient userClient) {
		this.ticketRepo = ticketRepo;
		this.ticketActivityRepo = ticketActivityRepo;
		this.ticketEventProducer = ticketEventProducer;
		this.userClient=userClient;
		

	}

	@Override
	public Mono<Ticket> createTicket(Ticket ticket) {
	    return ticketRepo.existsByCreatedByAndTitle(ticket.getCreatedBy(), ticket.getTitle())
	        .flatMap(exists -> {
	            if (exists) {
	                return Mono.error(new IllegalStateException("You already raised a ticket with this title"));
	            }
	            ticket.setStatus(STATUS.OPEN);
	            ticket.setCreatedAt(LocalDateTime.now());
	            ticket.setUpdatedAt(LocalDateTime.now());
	            ticket.setVersion(0L);

	            return ticketRepo.save(ticket)
	                .map(saved -> {
	                    String hex = saved.getId();
	                    String first2 = hex.substring(0, 2).toUpperCase();
	                    String last4 = hex.substring(hex.length() - 4).toUpperCase();
	                    saved.setDisplayId("TCK-" + first2 + last4);
	                    return saved;
	                })
	                .flatMap(ticketRepo::save)
	                .flatMap(saved -> logActivity(saved, ACTION_TYPE.CREATED))
	                .doOnSuccess(saved -> ticketEventProducer.publishTicketEvent(saved, "CREATED"));
	        });
	}


	@Override
	public Mono<Ticket> getTicketById(String id) {
		return ticketRepo.findById(id);
	}

	@Override
	public Flux<Ticket> getAllTickets() {
		return ticketRepo.findAll();
	}

	@Override
	public Mono<Ticket> updateTicketById(String id, Ticket updatedTicket) {
		return ticketRepo.findById(id).flatMap(existing -> {
			existing.setTitle(updatedTicket.getTitle());
			existing.setDescription(updatedTicket.getDescription());
			existing.setCategoryId(updatedTicket.getCategoryId());
			existing.setPriority(updatedTicket.getPriority());
			existing.setUpdatedAt(LocalDateTime.now());
			return ticketRepo.save(existing).flatMap(saved -> logActivity(saved, ACTION_TYPE.UPDATED));
		});
	}

	@Override
	public Mono<Ticket> closeTicket(String id) {
		return ticketRepo.findById(id).switchIfEmpty(Mono.error(new RuntimeException("Ticket not found")))
				.flatMap(ticket -> {
					if (ticket.getStatus() == STATUS.CLOSED) {
						return Mono.error(new RuntimeException("Ticket already closed"));
					}
					if (ticket.getStatus() == STATUS.ASSIGNED) {
						return Mono.error(new RuntimeException("Ticket is assigned cannot close"));
					}
					ticket.setStatus(STATUS.CLOSED);
					ticket.setUpdatedAt(LocalDateTime.now());
					return ticketRepo.save(ticket).flatMap(saved -> logActivity(saved, ACTION_TYPE.RESOLVED))
							.doOnSuccess(saved -> ticketEventProducer.publishTicketEvent(saved, "CLOSED"));
				});
	}

	@Override
	public Mono<Ticket> assignTicket(String id, String agentId) {
		return ticketRepo.findById(id).switchIfEmpty(Mono.error(new RuntimeException("Ticket not found")))
				.flatMap(ticket -> {

					if (ticket.getStatus() == STATUS.CLOSED) {
						return Mono.error(new RuntimeException("Cannot assign a closed ticket"));
					}
					if (ticket.getStatus() == STATUS.RESOLVED) {
						return Mono.error(new RuntimeException("Cannot assign a resolved ticket"));
					}
					ticket.setStatus(STATUS.ASSIGNED);
					ticket.setUpdatedAt(LocalDateTime.now());
					ticket.setAssignedTo(agentId);
					ticket.setVersion(ticket.getVersion() + 1);
					return ticketRepo.save(ticket).flatMap(saved -> logActivity(saved, ACTION_TYPE.ASSIGNED))
							.doOnSuccess(saved -> ticketEventProducer.publishTicketEvent(saved, "ASSIGNED"));
				});
	}

	@Override
	public Mono<Ticket> reopenTicket(String id) {
		return ticketRepo.findById(id).flatMap(ticket -> {
			if (ticket.getStatus() != STATUS.RESOLVED && ticket.getStatus() != STATUS.CLOSED) {
				return Mono.error(new IllegalArgumentException("Only RESOLVED or CLOSED tickets can be reopened"));
			}
			ticket.setStatus(STATUS.OPEN);
			ticket.setAssignedTo(null);
			ticket.setUpdatedAt(LocalDateTime.now());
			return ticketRepo.save(ticket).flatMap(saved -> logActivity(saved, ACTION_TYPE.REOPENED))
					.doOnSuccess(saved -> ticketEventProducer.publishTicketEvent(saved, "REOPENED"));
		});
	}

	@Override
	public Mono<Void> deleteTicket(String id) {
		return ticketRepo.deleteById(id);
	}

	@Override
	public Mono<Ticket> resolveTicket(String id) {
		return ticketRepo.findById(id).switchIfEmpty(Mono.error(new RuntimeException("Ticket not found")))
				.flatMap(ticket -> {
					if (ticket.getStatus() == STATUS.CLOSED) {
						return Mono.error(new RuntimeException("Ticket already closed"));
					}
					if (ticket.getStatus() == STATUS.RESOLVED) {
						return Mono.error(new RuntimeException("Ticket already resolved"));
					}
					if (ticket.getStatus() == STATUS.OPEN) {
						return Mono.error(new RuntimeException("Ticket must be assigned before resolving"));
					}
					ticket.setStatus(STATUS.RESOLVED);
					ticket.setVersion(0L);
					
					userClient.incrementResolvedCount(ticket.getAssignedTo());
					ticket.setAssignedTo(null);
					ticket.setUpdatedAt(LocalDateTime.now());
					
					return ticketRepo.save(ticket).flatMap(saved -> logActivity(saved, ACTION_TYPE.RESOLVED))
							.doOnSuccess(saved -> ticketEventProducer.publishTicketEvent(saved, "RESOLVED"));
				});
	}

	private Mono<Ticket> logActivity(Ticket ticket, ACTION_TYPE action) {
		String actorId=null;
		switch (action) {
		case CREATED:
			actorId = ticket.getCreatedBy();
			break;
		case ASSIGNED:
			actorId = ticket.getAssignedTo();
			break;
		case RESOLVED:
			actorId = ticket.getAssignedTo();
			break;
		case REOPENED:
			actorId = ticket.getCreatedBy();
			break;
		default:
			actorId = null;
		}
		TicketActivity activity = TicketActivity.builder().ticketId(ticket.getId()).actionType(action).actorId(actorId)
				.timestamp(Instant.now()).details("Ticket " + action).build();
		return ticketActivityRepo.save(activity).thenReturn(ticket);
	}

	@Override
	public Flux<Ticket> getTicketsByUserId(String userId) {
		return ticketRepo.findByCreatedBy(userId);
	}

	@Override
	public Mono<List<StatusSummaryDto>> statusSummary() {
		return ticketRepo.getStatusSummary().collectList();
	}

	@Override
	public Mono<List<PrioritySummaryDto>> prioritySummary() {
		return ticketRepo.getPrioritySummary().collectList();
	}

	@Override
	public Mono<List<CategorySummaryDto>> getCategorySummary() {
		return ticketRepo.getCategorySummary().collectList();
	}

	@Override
	public Mono<UserTicketStatsDto> getUserStats(String userId) {
		Mono<Long> total = ticketRepo.findByCreatedBy(userId).count();
		Mono<Long> open = ticketRepo.getStatusSummaryByUserId(userId).filter(s -> s.getStatus() == STATUS.OPEN)
				.map(StatusSummaryDto::getCount).next().defaultIfEmpty(0L);
		Mono<Long> resolved = ticketRepo.getStatusSummaryByUserId(userId).filter(s -> s.getStatus() == STATUS.RESOLVED)
				.map(StatusSummaryDto::getCount).next().defaultIfEmpty(0L);
		Mono<Long> critical = ticketRepo.getPrioritySummaryByUserId(userId)
				.filter(p -> p.getPriority() == PRIORITY.CRITICAL).map(PrioritySummaryDto::getCount).next()
				.defaultIfEmpty(0L);

		return Mono.zip(total, open, resolved, critical)
				.map(tuple -> new UserTicketStatsDto(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4()));
	}

	@Override
	public Mono<UserTicketStatsDto> getGlobalStats() {

		Mono<Long> total = ticketRepo.count();

		Mono<Long> open = ticketRepo.getStatusSummary().filter(s -> s.getStatus().equals(STATUS.OPEN))
				.map(StatusSummaryDto::getCount).next().defaultIfEmpty(0L);

		Mono<Long> resolved = ticketRepo.getStatusSummary().filter(s -> s.getStatus().equals(STATUS.RESOLVED))
				.map(StatusSummaryDto::getCount).next().defaultIfEmpty(0L);

		Mono<Long> critical = ticketRepo.getPrioritySummary().filter(p -> p.getPriority().equals(PRIORITY.CRITICAL))
				.map(PrioritySummaryDto::getCount).next().defaultIfEmpty(0L);

		return Mono.zip(total, open, resolved, critical)
				.map(tuple -> new UserTicketStatsDto(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4()));

	}

	@Override
	public Mono<List<Ticket>> getRecentTickets() {
		return ticketRepo.findTop5ByOrderByCreatedAtDesc().collectList();
	}

	@Override
	public Mono<List<Ticket>> getRecentTicketsByUser(String userId) {
		return ticketRepo.findTop5ByCreatedByOrderByCreatedAtDesc(userId).collectList();
	}

	@Override
	public Mono<List<Ticket>> getRecentTicketsByAgent(String agentId) {
		return ticketRepo.findTop5ByAssignedToOrderByCreatedAtDesc(agentId).collectList();
	}

	@Override
	public Mono<List<Ticket>> getTicketsByAgent(String agentId) {
		return ticketRepo.findByAssignedTo(agentId).collectList();
	}

	@Override
	public Mono<UserTicketStatsDto> getAgentStats(String agentId) {
		Mono<Long> total = ticketRepo.findByAssignedTo(agentId).count();
		Mono<Long> open = ticketRepo.getStatusSummaryByAgentId(agentId).filter(s -> s.getStatus() == STATUS.OPEN)
				.map(StatusSummaryDto::getCount).next().defaultIfEmpty(0L);
		Mono<Long> resolved = ticketRepo.getStatusSummaryByAgentId(agentId)
				.filter(s -> s.getStatus() == STATUS.RESOLVED).map(StatusSummaryDto::getCount).next()
				.defaultIfEmpty(0L);
		Mono<Long> critical = ticketRepo.getPrioritySummaryByAgentId(agentId)
				.filter(p -> p.getPriority() == PRIORITY.CRITICAL).map(PrioritySummaryDto::getCount).next()
				.defaultIfEmpty(0L);

		return Mono.zip(total, open, resolved, critical)
				.map(tuple -> new UserTicketStatsDto(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4()));
	}

	@Override
	public Mono<Ticket> startWorkOnTicket(String id, String agentId) {
		return ticketRepo.findById(id).switchIfEmpty(Mono.error(new RuntimeException("Ticket not found")))
				.flatMap(ticket -> {
					if (ticket.getStatus() == STATUS.CLOSED) {
						return Mono.error(new RuntimeException("Cannot start work on a closed ticket"));
					}
					if (ticket.getStatus() == STATUS.RESOLVED) {
						return Mono.error(new RuntimeException("Cannot start work on a resolved ticket"));
					}
					if (ticket.getStatus() == STATUS.OPEN) {
						return Mono.error(new RuntimeException("Ticket must be assigned before starting work"));
					}
					if (ticket.getStatus() == STATUS.IN_PROGRESS) {
						return Mono.error(new RuntimeException("Ticket is already in progress"));
					}

					ticket.setStatus(STATUS.IN_PROGRESS);
					ticket.setUpdatedAt(LocalDateTime.now());
					ticket.setAssignedTo(agentId);

					return ticketRepo.save(ticket).flatMap(saved -> logActivity(saved, ACTION_TYPE.IN_PROGRESS))
							.doOnSuccess(saved -> ticketEventProducer.publishTicketEvent(saved, "IN_PROGRESS"));
				});
	}

	@Override
	public Mono<Ticket> escalateTicket(String id) {
		return ticketRepo.findById(id).switchIfEmpty(Mono.error(new RuntimeException("Ticket not found")))
				.flatMap(ticket -> {
					if (ticket.getStatus() == STATUS.CLOSED) {
						return Mono.error(new RuntimeException("Ticket already closed"));
					}
					if (ticket.getStatus() == STATUS.RESOLVED) {
						return Mono.error(new RuntimeException("Ticket already resolved"));
					}
					if (ticket.getStatus() == STATUS.OPEN) {
						return Mono.error(new RuntimeException("Ticket must be assigned before escalating"));
					}
					ticket.setStatus(STATUS.ESCALATED);
					ticket.setVersion(0L);
					userClient.decrementAssignments(ticket.getAssignedTo());
					userClient.incrementEscalatedCount(ticket.getAssignedTo());
					ticket.setAssignedTo(null);
					ticket.setUpdatedAt(LocalDateTime.now());
					
					return ticketRepo.save(ticket).flatMap(saved -> logActivity(saved, ACTION_TYPE.ESCALATED))
							.doOnSuccess(saved -> ticketEventProducer.publishTicketEvent(saved, "ESCALATED"));
				});
	}
}
