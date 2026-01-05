package com.smartticket.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartticket.demo.dto.CategorySummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.entity.Ticket;
import com.smartticket.demo.entity.TicketActivity;
import com.smartticket.demo.enums.PRIORITY;
import com.smartticket.demo.enums.STATUS;
import com.smartticket.demo.feign.UserClient;
import com.smartticket.demo.producer.TicketEventProducer;
import com.smartticket.demo.repository.TicketActivityRepository;
import com.smartticket.demo.repository.TicketRepository;
import com.smartticket.demo.service.implementation.TicketServiceImplementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class TicketServiceImplementationTest {
	@Mock
	private TicketRepository ticketRepo;
	@Mock
	private TicketActivityRepository ticketActivityRepo;
	@Mock
	private TicketEventProducer ticketEventProducer;
	@Mock
	private UserClient userClient;
	@InjectMocks
	private TicketServiceImplementation service;

	@Test
	void createTicket_success() {
		Ticket ticket = Ticket.builder().id("T1123456").createdBy("U1").title("New Ticket").build();
		Ticket saved = Ticket.builder().id("T1123456").createdBy("U1").title("New Ticket").status(STATUS.OPEN).build();
		when(ticketRepo.existsByCreatedByAndTitle("U1", "New Ticket")).thenReturn(Mono.just(false));
		when(ticketRepo.save(any(Ticket.class))).thenReturn(Mono.just(saved));
		when(ticketActivityRepo.save(any(TicketActivity.class))).thenReturn(Mono.empty());
		StepVerifier.create(service.createTicket(ticket)).assertNext(result -> {
			assertEquals(STATUS.OPEN, result.getStatus());
			assertTrue(result.getDisplayId().startsWith("TCK-"));
		}).verifyComplete();
	}

	@Test
	void getTicketById_success() {
		Ticket ticket = Ticket.builder().id("T1").title("Test").build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(ticket));
		StepVerifier.create(service.getTicketById("T1")).assertNext(result -> assertEquals("Test", result.getTitle()))
				.verifyComplete();
	}

	@Test
	void getAllTickets_success() {
		Ticket t1 = Ticket.builder().id("T1").title("A").build();
		Ticket t2 = Ticket.builder().id("T2").title("B").build();
		when(ticketRepo.findAll()).thenReturn(Flux.just(t1, t2));
		StepVerifier.create(service.getAllTickets()).expectNext(t1).expectNext(t2).verifyComplete();
	}

	@Test
	void updateTicket_success() {
		Ticket existing = Ticket.builder().id("T1").title("Old").status(STATUS.OPEN).build();
		Ticket updated = Ticket.builder().id("T1").title("New").status(STATUS.OPEN).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(existing));
		when(ticketRepo.save(any(Ticket.class))).thenReturn(Mono.just(updated));
		when(ticketActivityRepo.save(any(TicketActivity.class))).thenReturn(Mono.empty());
		StepVerifier.create(service.updateTicketById("T1", updated))
				.assertNext(result -> assertEquals("New", result.getTitle())).verifyComplete();
	}

	@Test
	void closeTicket_success() {
		Ticket assigned = Ticket.builder().id("T1").status(STATUS.ASSIGNED).build();
		Ticket closed = Ticket.builder().id("T1").status(STATUS.CLOSED).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(assigned));
		when(ticketRepo.save(any(Ticket.class))).thenReturn(Mono.just(closed));
		when(ticketActivityRepo.save(any(TicketActivity.class))).thenReturn(Mono.empty());
		StepVerifier.create(service.closeTicket("T1"))
				.assertNext(result -> assertEquals(STATUS.CLOSED, result.getStatus())).verifyComplete();
	}

	@Test
	void assignTicket_success() {
		Ticket open = Ticket.builder().id("T1").status(STATUS.OPEN).version(0L).build();
		Ticket assigned = Ticket.builder().id("T1").status(STATUS.ASSIGNED).assignedTo("A1").version(1L).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(open));
		when(ticketRepo.save(any(Ticket.class))).thenReturn(Mono.just(assigned));
		when(ticketActivityRepo.save(any(TicketActivity.class))).thenReturn(Mono.empty());
		StepVerifier.create(service.assignTicket("T1", "A1")).assertNext(result -> {
			assertEquals(STATUS.ASSIGNED, result.getStatus());
			assertEquals("A1", result.getAssignedTo());
		}).verifyComplete();
	}

	@Test
	void reopenTicket_success() {
		Ticket closed = Ticket.builder().id("T1").status(STATUS.CLOSED).build();
		Ticket reopened = Ticket.builder().id("T1").status(STATUS.OPEN).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(closed));
		when(ticketRepo.save(any(Ticket.class))).thenReturn(Mono.just(reopened));
		when(ticketActivityRepo.save(any(TicketActivity.class))).thenReturn(Mono.empty());
		StepVerifier.create(service.reopenTicket("T1"))
				.assertNext(result -> assertEquals(STATUS.OPEN, result.getStatus())).verifyComplete();
	}

	@Test
	void resolveTicket_success() {
		Ticket assigned = Ticket.builder().id("T1").status(STATUS.ASSIGNED).assignedTo("A1").build();
		Ticket resolved = Ticket.builder().id("T1").status(STATUS.RESOLVED).assignedTo("A1").build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(assigned));
		when(ticketRepo.save(any(Ticket.class))).thenReturn(Mono.just(resolved));
		when(ticketActivityRepo.save(any(TicketActivity.class))).thenReturn(Mono.empty());
		StepVerifier.create(service.resolveTicket("T1"))
				.assertNext(result -> assertEquals(STATUS.RESOLVED, result.getStatus())).verifyComplete();
	}

	@Test
	void startWork_success() {
		Ticket assigned = Ticket.builder().id("T1").status(STATUS.ASSIGNED).build();
		Ticket inProgress = Ticket.builder().id("T1").status(STATUS.IN_PROGRESS).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(assigned));
		when(ticketRepo.save(any(Ticket.class))).thenReturn(Mono.just(inProgress));
		when(ticketActivityRepo.save(any(TicketActivity.class))).thenReturn(Mono.empty());
		StepVerifier.create(service.startWorkOnTicket("T1", "A1"))
				.assertNext(result -> assertEquals(STATUS.IN_PROGRESS, result.getStatus())).verifyComplete();
	}

	@Test
	void statusSummary_success() {
		StatusSummaryDto s1 = new StatusSummaryDto(STATUS.OPEN, 3L);
		StatusSummaryDto s2 = new StatusSummaryDto(STATUS.RESOLVED, 5L);
		when(ticketRepo.getStatusSummary()).thenReturn(Flux.just(s1, s2));
		StepVerifier.create(service.statusSummary()).assertNext(list -> {
			assertEquals(2, list.size());
			assertEquals(STATUS.OPEN, list.get(0).getStatus());
			assertEquals(3L, list.get(0).getCount());
		}).verifyComplete();
	}

	@Test
	void prioritySummary_success() {
		PrioritySummaryDto p1 = new PrioritySummaryDto(PRIORITY.HIGH, 2L);
		PrioritySummaryDto p2 = new PrioritySummaryDto(PRIORITY.CRITICAL, 1L);
		when(ticketRepo.getPrioritySummary()).thenReturn(Flux.just(p1, p2));
		StepVerifier.create(service.prioritySummary()).assertNext(list -> {
			assertEquals(2, list.size());
			assertEquals(PRIORITY.HIGH, list.get(0).getPriority());
			assertEquals(2L, list.get(0).getCount());
		}).verifyComplete();
	}

	@Test
	void categorySummary_success() {
		CategorySummaryDto c1 = new CategorySummaryDto("C1", 4L);
		CategorySummaryDto c2 = new CategorySummaryDto("C2", 6L);
		when(ticketRepo.getCategorySummary()).thenReturn(Flux.just(c1, c2));
		StepVerifier.create(service.getCategorySummary()).assertNext(list -> {
			assertEquals(2, list.size());
			assertEquals("C1", list.get(0).getCategoryId());
			assertEquals(4L, list.get(0).getCount());
		}).verifyComplete();
	}

	@Test
	void getUserStats_success() {
		StatusSummaryDto open = new StatusSummaryDto(STATUS.OPEN, 2L);
		StatusSummaryDto resolved = new StatusSummaryDto(STATUS.RESOLVED, 3L);
		PrioritySummaryDto critical = new PrioritySummaryDto(PRIORITY.CRITICAL, 1L);
		when(ticketRepo.findByCreatedBy("U1")).thenReturn(Flux.just(new Ticket(), new Ticket(), new Ticket()));
		when(ticketRepo.getStatusSummaryByUserId("U1")).thenReturn(Flux.just(open, resolved));
		when(ticketRepo.getPrioritySummaryByUserId("U1")).thenReturn(Flux.just(critical));
		StepVerifier.create(service.getUserStats("U1")).assertNext(stats -> {
			assertEquals(3L, stats.getResolved());
			assertEquals(2L, stats.getOpen());
			assertEquals(3L, stats.getTotal());
			assertEquals(1L, stats.getCritical());
		}).verifyComplete();
	}

	@Test
	void getGlobalStats_success() {
		StatusSummaryDto open = new StatusSummaryDto(STATUS.OPEN, 5L);
		StatusSummaryDto resolved = new StatusSummaryDto(STATUS.RESOLVED, 7L);
		PrioritySummaryDto critical = new PrioritySummaryDto(PRIORITY.CRITICAL, 2L);
		when(ticketRepo.count()).thenReturn(Mono.just(20L));
		when(ticketRepo.getStatusSummary()).thenReturn(Flux.just(open, resolved));
		when(ticketRepo.getPrioritySummary()).thenReturn(Flux.just(critical));
		StepVerifier.create(service.getGlobalStats()).assertNext(stats -> {
			assertEquals(20L, stats.getTotal());
			assertEquals(5L, stats.getOpen());
			assertEquals(7L, stats.getResolved());
			assertEquals(2L, stats.getCritical());
		}).verifyComplete();
	}

	@Test
	void getRecentTickets_success() {
		Ticket t1 = Ticket.builder().id("T1").title("A").build();
		Ticket t2 = Ticket.builder().id("T2").title("B").build();
		when(ticketRepo.findTop5ByOrderByCreatedAtDesc()).thenReturn(Flux.just(t1, t2));
		StepVerifier.create(service.getRecentTickets()).assertNext(list -> {
			assertEquals(2, list.size());
			assertEquals("T1", list.get(0).getId());
		}).verifyComplete();
	}

	@Test
	void getRecentTicketsByUser_success() {
		Ticket t1 = Ticket.builder().id("T1").title("A").build();
		when(ticketRepo.findTop5ByCreatedByOrderByCreatedAtDesc("U1")).thenReturn(Flux.just(t1));
		StepVerifier.create(service.getRecentTicketsByUser("U1")).assertNext(list -> {
			assertEquals(1, list.size());
			assertEquals("T1", list.get(0).getId());
		}).verifyComplete();
	}

	@Test
	void getRecentTicketsByAgent_success() {
		Ticket t1 = Ticket.builder().id("T1").title("A").build();
		when(ticketRepo.findTop5ByAssignedToOrderByCreatedAtDesc("A1")).thenReturn(Flux.just(t1));
		StepVerifier.create(service.getRecentTicketsByAgent("A1")).assertNext(list -> {
			assertEquals(1, list.size());
			assertEquals("T1", list.get(0).getId());
		}).verifyComplete();
	}

	@Test
	void getTicketsByAgent_success() {
		Ticket t1 = Ticket.builder().id("T1").title("A").build();
		Ticket t2 = Ticket.builder().id("T2").title("B").build();
		when(ticketRepo.findByAssignedTo("A1")).thenReturn(Flux.just(t1, t2));
		StepVerifier.create(service.getTicketsByAgent("A1")).assertNext(list -> {
			assertEquals(2, list.size());
			assertEquals("T1", list.get(0).getId());
		}).verifyComplete();
	}

	@Test
	void getAgentStats_success() {
		StatusSummaryDto open = new StatusSummaryDto(STATUS.OPEN, 1L);
		StatusSummaryDto resolved = new StatusSummaryDto(STATUS.RESOLVED, 2L);
		PrioritySummaryDto critical = new PrioritySummaryDto(PRIORITY.CRITICAL, 1L);
		when(ticketRepo.findByAssignedTo("A1")).thenReturn(Flux.just(new Ticket(), new Ticket()));
		when(ticketRepo.getStatusSummaryByAgentId("A1")).thenReturn(Flux.just(open, resolved));
		when(ticketRepo.getPrioritySummaryByAgentId("A1")).thenReturn(Flux.just(critical));
		StepVerifier.create(service.getAgentStats("A1")).assertNext(stats -> {
			assertEquals(2L, stats.getTotal());
			assertEquals(1L, stats.getOpen());
			assertEquals(2L, stats.getResolved());
			assertEquals(1L, stats.getCritical());
		}).verifyComplete();
	}

	@Test
	void statusSummary_empty() {
		when(ticketRepo.getStatusSummary()).thenReturn(Flux.empty());
		StepVerifier.create(service.statusSummary()).assertNext(list -> assertTrue(list.isEmpty())).verifyComplete();
	}

	@Test
	void prioritySummary_empty() {
		when(ticketRepo.getPrioritySummary()).thenReturn(Flux.empty());
		StepVerifier.create(service.prioritySummary()).assertNext(list -> assertTrue(list.isEmpty())).verifyComplete();
	}

	@Test
	void categorySummary_empty() {
		when(ticketRepo.getCategorySummary()).thenReturn(Flux.empty());
		StepVerifier.create(service.getCategorySummary()).assertNext(list -> assertTrue(list.isEmpty()))
				.verifyComplete();
	}

	@Test
	void getUserStats_emptySummaries() {
		when(ticketRepo.findByCreatedBy("U1")).thenReturn(Flux.empty());
		when(ticketRepo.getStatusSummaryByUserId("U1")).thenReturn(Flux.empty());
		when(ticketRepo.getPrioritySummaryByUserId("U1")).thenReturn(Flux.empty());
		StepVerifier.create(service.getUserStats("U1")).assertNext(stats -> {
			assertEquals(0L, stats.getTotal());
			assertEquals(0L, stats.getOpen());
			assertEquals(0L, stats.getResolved());
			assertEquals(0L, stats.getCritical());
		}).verifyComplete();
	}

	@Test
	void getGlobalStats_emptySummaries() {
		when(ticketRepo.count()).thenReturn(Mono.just(0L));
		when(ticketRepo.getStatusSummary()).thenReturn(Flux.empty());
		when(ticketRepo.getPrioritySummary()).thenReturn(Flux.empty());
		StepVerifier.create(service.getGlobalStats()).assertNext(stats -> {
			assertEquals(0L, stats.getTotal());
			assertEquals(0L, stats.getOpen());
			assertEquals(0L, stats.getResolved());
			assertEquals(0L, stats.getCritical());
		}).verifyComplete();
	}

	@Test
	void getRecentTickets_empty() {
		when(ticketRepo.findTop5ByOrderByCreatedAtDesc()).thenReturn(Flux.empty());
		StepVerifier.create(service.getRecentTickets()).assertNext(list -> assertTrue(list.isEmpty())).verifyComplete();
	}

	@Test
	void getRecentTicketsByUser_empty() {
		when(ticketRepo.findTop5ByCreatedByOrderByCreatedAtDesc("U1")).thenReturn(Flux.empty());
		StepVerifier.create(service.getRecentTicketsByUser("U1")).assertNext(list -> assertTrue(list.isEmpty()))
				.verifyComplete();
	}

	@Test
	void getRecentTicketsByAgent_empty() {
		when(ticketRepo.findTop5ByAssignedToOrderByCreatedAtDesc("A1")).thenReturn(Flux.empty());
		StepVerifier.create(service.getRecentTicketsByAgent("A1")).assertNext(list -> assertTrue(list.isEmpty()))
				.verifyComplete();
	}

	@Test
	void getTicketsByAgent_empty() {
		when(ticketRepo.findByAssignedTo("A1")).thenReturn(Flux.empty());
		StepVerifier.create(service.getTicketsByAgent("A1")).assertNext(list -> assertTrue(list.isEmpty()))
				.verifyComplete();
	}

	@Test
	void getAgentStats_emptySummaries() {
		when(ticketRepo.findByAssignedTo("A1")).thenReturn(Flux.empty());
		when(ticketRepo.getStatusSummaryByAgentId("A1")).thenReturn(Flux.empty());
		when(ticketRepo.getPrioritySummaryByAgentId("A1")).thenReturn(Flux.empty());
		StepVerifier.create(service.getAgentStats("A1")).assertNext(stats -> {
			assertEquals(0L, stats.getTotal());
			assertEquals(0L, stats.getOpen());
			assertEquals(0L, stats.getResolved());
			assertEquals(0L, stats.getCritical());
		}).verifyComplete();
	}

	@Test
	void createTicket_duplicateTitle() {
		Ticket ticket = Ticket.builder().id("T1").createdBy("U1").title("Duplicate").build();
		when(ticketRepo.existsByCreatedByAndTitle("U1", "Duplicate")).thenReturn(Mono.just(true));
		StepVerifier.create(service.createTicket(ticket)).expectErrorMatches(ex -> ex instanceof IllegalStateException
				&& ex.getMessage().equals("You already raised a ticket with this title")).verify();
	}

	@Test
	void updateTicket_notFound() {
		Ticket updated = Ticket.builder().id("T1").title("New").build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.empty());
		StepVerifier.create(service.updateTicketById("T1", updated)).verifyComplete();
	}

	@Test
	void closeTicket_notFound() {
		when(ticketRepo.findById("T1")).thenReturn(Mono.empty());
		StepVerifier.create(service.closeTicket("T1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Ticket not found")).verify();
	}

	@Test
	void closeTicket_alreadyClosed() {
		Ticket ticket = Ticket.builder().id("T1").status(STATUS.CLOSED).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(ticket));
		StepVerifier.create(service.closeTicket("T1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Ticket already closed")).verify();
	}

	@Test
	void closeTicket_openTicket() {
		Ticket ticket = Ticket.builder().id("T1").status(STATUS.OPEN).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(ticket));
		StepVerifier.create(service.closeTicket("T1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Ticket must be assigned before closing")).verify();
	}

	@Test
	void assignTicket_notFound() {
		when(ticketRepo.findById("T1")).thenReturn(Mono.empty());
		StepVerifier.create(service.assignTicket("T1", "A1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Ticket not found")).verify();
	}

	@Test
	void assignTicket_closedTicket() {
		Ticket ticket = Ticket.builder().id("T1").status(STATUS.CLOSED).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(ticket));
		StepVerifier.create(service.assignTicket("T1", "A1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Cannot assign a closed ticket")).verify();
	}

	@Test
	void assignTicket_resolvedTicket() {
		Ticket ticket = Ticket.builder().id("T1").status(STATUS.RESOLVED).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(ticket));
		StepVerifier.create(service.assignTicket("T1", "A1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Cannot assign a resolved ticket")).verify();
	}

	@Test
	void reopenTicket_invalidStatus() {
		Ticket ticket = Ticket.builder().id("T1").status(STATUS.OPEN).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(ticket));
		StepVerifier.create(service.reopenTicket("T1")).expectErrorMatches(
				ex -> ex instanceof IllegalArgumentException && ex.getMessage().contains("Only RESOLVED or CLOSED"))
				.verify();
	}

	@Test
	void resolveTicket_notFound() {
		when(ticketRepo.findById("T1")).thenReturn(Mono.empty());
		StepVerifier.create(service.resolveTicket("T1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Ticket not found")).verify();
	}

	@Test
	void resolveTicket_alreadyClosed() {
		Ticket ticket = Ticket.builder().id("T1").status(STATUS.CLOSED).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(ticket));
		StepVerifier.create(service.resolveTicket("T1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Ticket already closed")).verify();
	}

	@Test
	void resolveTicket_alreadyResolved() {
		Ticket ticket = Ticket.builder().id("T1").status(STATUS.RESOLVED).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(ticket));
		StepVerifier.create(service.resolveTicket("T1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Ticket already resolved")).verify();
	}

	@Test
	void resolveTicket_openTicket() {
		Ticket ticket = Ticket.builder().id("T1").status(STATUS.OPEN).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(ticket));
		StepVerifier.create(service.resolveTicket("T1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Ticket must be assigned before resolving")).verify();
	}

	@Test
	void startWork_notFound() {
		when(ticketRepo.findById("T1")).thenReturn(Mono.empty());
		StepVerifier.create(service.startWorkOnTicket("T1", "A1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Ticket not found")).verify();
	}

	@Test
	void startWork_closedTicket() {
		Ticket ticket = Ticket.builder().id("T1").status(STATUS.CLOSED).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(ticket));
		StepVerifier.create(service.startWorkOnTicket("T1", "A1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Cannot start work on a closed ticket")).verify();
	}

	@Test
	void startWork_resolvedTicket() {
		Ticket ticket = Ticket.builder().id("T1").status(STATUS.RESOLVED).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(ticket));
		StepVerifier.create(service.startWorkOnTicket("T1", "A1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Cannot start work on a resolved ticket")).verify();
	}

	@Test
	void startWork_openTicket() {
		Ticket ticket = Ticket.builder().id("T1").status(STATUS.OPEN).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(ticket));
		StepVerifier.create(service.startWorkOnTicket("T1", "A1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Ticket must be assigned before starting work"))
				.verify();
	}

	@Test
	void startWork_alreadyInProgress() {
		Ticket ticket = Ticket.builder().id("T1").status(STATUS.IN_PROGRESS).build();
		when(ticketRepo.findById("T1")).thenReturn(Mono.just(ticket));
		StepVerifier.create(service.startWorkOnTicket("T1", "A1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Ticket is already in progress")).verify();
	}

}
