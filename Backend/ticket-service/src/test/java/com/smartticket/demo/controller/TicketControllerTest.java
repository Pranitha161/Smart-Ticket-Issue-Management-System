package com.smartticket.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartticket.demo.dto.CategorySummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.dto.UserTicketStatsDto;
import com.smartticket.demo.entity.Ticket;
import com.smartticket.demo.enums.STATUS;
import com.smartticket.demo.service.implementation.TicketServiceImplementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class TicketControllerTest {
	@Mock
	private TicketServiceImplementation ticketService;
	@InjectMocks
	private TicketController controller;

	@Test
	void createTicket_success() {
		Ticket ticket = Ticket.builder().id("T1").displayId("TCK-AB12").build();
		when(ticketService.createTicket(ticket)).thenReturn(Mono.just(ticket));
		StepVerifier.create(controller.createTicket(ticket)).assertNext(response -> {
			assertEquals(201, response.getStatusCodeValue());
			assertTrue(response.getBody().isSuccess());
			assertTrue(response.getBody().getMessage().contains("Ticket created successfully"));
		}).verifyComplete();
	}

	@Test
	void getTicketById_success() {
		Ticket ticket = Ticket.builder().id("T1").title("Test").build();
		when(ticketService.getTicketById("T1")).thenReturn(Mono.just(ticket));
		StepVerifier.create(controller.getTicketById("T1")).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertEquals("Test", response.getBody().getTitle());
		}).verifyComplete();
	}

	@Test
	void getTicketById_notFound() {
		when(ticketService.getTicketById("T1")).thenReturn(Mono.empty());
		StepVerifier.create(controller.getTicketById("T1"))
				.assertNext(response -> assertEquals(404, response.getStatusCodeValue())).verifyComplete();
	}

	@Test
	void getAllTickets_success() {
		Ticket t1 = Ticket.builder().id("T1").title("A").build();
		Ticket t2 = Ticket.builder().id("T2").title("B").build();
		when(ticketService.getAllTickets()).thenReturn(Flux.just(t1, t2));
		StepVerifier.create(controller.getAllTickets()).expectNext(t1).expectNext(t2).verifyComplete();
	}

	@Test
	void updateTicket_success() {
		Ticket updated = Ticket.builder().id("T1").title("New").build();
		when(ticketService.updateTicketById("T1", updated)).thenReturn(Mono.just(updated));
		StepVerifier.create(controller.updateTicket("T1", updated)).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertTrue(response.getBody().isSuccess());
		}).verifyComplete();
	}

	@Test
	void updateTicket_notFound() {
		Ticket updated = Ticket.builder().id("T1").title("New").build();
		when(ticketService.updateTicketById("T1", updated)).thenReturn(Mono.empty());
		StepVerifier.create(controller.updateTicket("T1", updated)).assertNext(response -> {
			assertEquals(404, response.getStatusCodeValue());
			assertFalse(response.getBody().isSuccess());
			assertEquals("Ticket not found", response.getBody().getMessage());
		}).verifyComplete();
	}

	@Test
	void closeTicket_success() {
		Ticket closed = Ticket.builder().id("T1").status(STATUS.CLOSED).build();
		when(ticketService.closeTicket("T1")).thenReturn(Mono.just(closed));
		StepVerifier.create(controller.closeTicket("T1")).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertTrue(response.getBody().isSuccess());
		}).verifyComplete();
	}

	@Test
	void closeTicket_notFound() {
		when(ticketService.closeTicket("T1")).thenReturn(Mono.empty());
		StepVerifier.create(controller.closeTicket("T1")).assertNext(response -> {
			assertEquals(404, response.getStatusCodeValue());
			assertFalse(response.getBody().isSuccess());
		}).verifyComplete();
	}

	@Test
	void resolveTicket_success() {
		Ticket resolved = Ticket.builder().id("T1").status(STATUS.RESOLVED).build();
		when(ticketService.resolveTicket("T1")).thenReturn(Mono.just(resolved));
		StepVerifier.create(controller.resolveTicket("T1")).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertTrue(response.getBody().isSuccess());
		}).verifyComplete();
	}

	@Test
	void reopenTicket_success() {
		Ticket reopened = Ticket.builder().id("T1").status(STATUS.OPEN).build();
		when(ticketService.reopenTicket("T1")).thenReturn(Mono.just(reopened));
		StepVerifier.create(controller.reopenTicket("T1")).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertTrue(response.getBody().isSuccess());
		}).verifyComplete();
	}

	@Test
	void deleteTicket_success() {
		when(ticketService.deleteTicket("T1")).thenReturn(Mono.empty());
		StepVerifier.create(controller.deleteTicket("T1")).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertTrue(response.getBody().isSuccess());
			assertEquals("Ticket deleted successfully", response.getBody().getMessage());
		}).verifyComplete();
	}

	@Test
	void statusSummary_success() {
		StatusSummaryDto s1 = new StatusSummaryDto(STATUS.OPEN, 2L);
		when(ticketService.statusSummary()).thenReturn(Mono.just(List.of(s1)));
		StepVerifier.create(controller.getTicketStatusSummary()).assertNext(list -> {
			assertEquals(1, list.size());
			assertEquals(STATUS.OPEN, list.get(0).getStatus());
		}).verifyComplete();
	}

	@Test
	void prioritySummary_success() {
		PrioritySummaryDto p1 = new PrioritySummaryDto(com.smartticket.demo.enums.PRIORITY.HIGH, 1L);
		when(ticketService.prioritySummary()).thenReturn(Mono.just(List.of(p1)));
		StepVerifier.create(controller.getTicketStatusPrioritySummary()).assertNext(list -> {
			assertEquals(1, list.size());
			assertEquals(com.smartticket.demo.enums.PRIORITY.HIGH, list.get(0).getPriority());
		}).verifyComplete();
	}

	@Test
	void categorySummary_success() {
		CategorySummaryDto c1 = new CategorySummaryDto("C1", 3L);
		when(ticketService.getCategorySummary()).thenReturn(Mono.just(List.of(c1)));
		StepVerifier.create(controller.getCategorySummary()).assertNext(list -> {
			assertEquals(1, list.size());
			assertEquals("C1", list.get(0).getCategoryId());
		}).verifyComplete();
	}

	@Test
	void getUserStats_success() {
		UserTicketStatsDto stats = new UserTicketStatsDto(5L, 2L, 3L, 1L);
		when(ticketService.getUserStats("U1")).thenReturn(Mono.just(stats));
		StepVerifier.create(controller.getUserStats("U1")).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertEquals(5L, response.getBody().getTotal());
		}).verifyComplete();
	}

	@Test
	void getAgentStats_success() {
		UserTicketStatsDto stats = new UserTicketStatsDto(4L, 1L, 2L, 1L);
		when(ticketService.getAgentStats("A1")).thenReturn(Mono.just(stats));
		StepVerifier.create(controller.getAgentStats("A1")).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertEquals(4L, response.getBody().getTotal());
		}).verifyComplete();
	}

	@Test
	void getGlobalStats_success() {
		UserTicketStatsDto stats = new UserTicketStatsDto(10L, 3L, 5L, 2L);
		when(ticketService.getGlobalStats()).thenReturn(Mono.just(stats));
		StepVerifier.create(controller.getGlobalStats()).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertEquals(10L, response.getBody().getTotal());
		}).verifyComplete();
	}

	@Test
	void startWorkOnTicket_success() {
		Ticket inProgress = Ticket.builder().id("T1").status(STATUS.IN_PROGRESS).build();
		when(ticketService.startWorkOnTicket("T1", "A1")).thenReturn(Mono.just(inProgress));
		StepVerifier.create(controller.startWorkOnTicket("T1", "A1")).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertEquals(STATUS.IN_PROGRESS, response.getBody().getStatus());
		}).verifyComplete();
	}

	@Test
	void startWorkOnTicket_notFound() {
		when(ticketService.startWorkOnTicket("T1", "A1")).thenReturn(Mono.empty());
		StepVerifier.create(controller.startWorkOnTicket("T1", "A1")).verifyComplete();
	}

	@Test
	void getTicketsByUserId_success() {
		Ticket t1 = Ticket.builder().id("T1").title("A").build();
		when(ticketService.getTicketsByUserId("U1")).thenReturn(Flux.just(t1));
		StepVerifier.create(controller.getTicketsByUserId("U1")).expectNext(t1).verifyComplete();
	}

	@Test
	void getRecentTickets_success() {
		Ticket t1 = Ticket.builder().id("T1").title("A").build();
		when(ticketService.getRecentTickets()).thenReturn(Mono.just(List.of(t1)));
		StepVerifier.create(controller.getRecentTickets()).assertNext(list -> {
			assertEquals(1, list.size());
			assertEquals("T1", list.get(0).getId());
		}).verifyComplete();
	}

	@Test
	void getRecentTicketsByUser_success() {
		Ticket t1 = Ticket.builder().id("T1").title("A").build();
		when(ticketService.getRecentTicketsByUser("U1")).thenReturn(Mono.just(List.of(t1)));
		StepVerifier.create(controller.getRecentTicketsByUser("U1")).assertNext(list -> {
			assertEquals(1, list.size());
			assertEquals("T1", list.get(0).getId());
		}).verifyComplete();
	}

	@Test
	void getRecentTicketsByAgent_success() {
		Ticket t1 = Ticket.builder().id("T1").title("A").build();
		when(ticketService.getRecentTicketsByAgent("A1")).thenReturn(Mono.just(List.of(t1)));
		StepVerifier.create(controller.getRecentTicketsByAgent("A1")).assertNext(list -> {
			assertEquals(1, list.size());
			assertEquals("T1", list.get(0).getId());
		}).verifyComplete();
	}

	@Test
	void getTicketsByAgent_success() {
		Ticket t1 = Ticket.builder().id("T1").title("A").build();
		Ticket t2 = Ticket.builder().id("T2").title("B").build();
		when(ticketService.getTicketsByAgent("A1")).thenReturn(Mono.just(List.of(t1, t2)));
		StepVerifier.create(controller.getTicketsByAgent("A1")).assertNext(list -> {
			assertEquals(2, list.size());
			assertEquals("T1", list.get(0).getId());
		}).verifyComplete();
	}

	@Test
	void assignTicket_success() {
		Ticket assigned = Ticket.builder().id("T1").status(STATUS.ASSIGNED).build();
		when(ticketService.assignTicket("T1", "A1")).thenReturn(Mono.just(assigned));
		StepVerifier.create(controller.assignTicket("T1", "A1")).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertTrue(response.getBody().isSuccess());
			assertEquals("Ticket assigned successfully", response.getBody().getMessage());
		}).verifyComplete();
	}

	@Test
	void assignTicket_notFound() {
		when(ticketService.assignTicket("T1", "A1")).thenReturn(Mono.empty());
		StepVerifier.create(controller.assignTicket("T1", "A1")).assertNext(response -> {
			assertEquals(404, response.getStatusCodeValue());
			assertFalse(response.getBody().isSuccess());
			assertEquals("Ticket not found", response.getBody().getMessage());
		}).verifyComplete();
	}

	@Test
	void escalateTicket_success() {
		Ticket escalated = Ticket.builder().id("T1").status(STATUS.ESCALATED).build();
		when(ticketService.escalateTicket("T1")).thenReturn(Mono.just(escalated));

		StepVerifier.create(controller.escalateTicket("T1")).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertTrue(response.getBody().isSuccess());
			assertEquals("Ticket escalated successfully", response.getBody().getMessage());
		}).verifyComplete();
	}

	@Test
	void escalateTicket_notFound() {
		when(ticketService.escalateTicket("T1")).thenReturn(Mono.empty());

		StepVerifier.create(controller.escalateTicket("T1")).assertNext(response -> {
			assertEquals(404, response.getStatusCodeValue());
			assertFalse(response.getBody().isSuccess());
			assertEquals("Ticket not found", response.getBody().getMessage());
		}).verifyComplete();
	}

	@Test
	void getUserStats_notFound() {
		when(ticketService.getUserStats("U1")).thenReturn(Mono.empty());

		StepVerifier.create(controller.getUserStats("U1"))
				.assertNext(response -> assertEquals(404, response.getStatusCodeValue())).verifyComplete();
	}

	@Test
	void getAgentStats_notFound() {
		when(ticketService.getAgentStats("A1")).thenReturn(Mono.empty());

		StepVerifier.create(controller.getAgentStats("A1"))
				.assertNext(response -> assertEquals(404, response.getStatusCodeValue())).verifyComplete();
	}

	@Test
	void getGlobalStats_notFound() {
		when(ticketService.getGlobalStats()).thenReturn(Mono.empty());

		StepVerifier.create(controller.getGlobalStats())
				.assertNext(response -> assertEquals(404, response.getStatusCodeValue())).verifyComplete();
	}

}
