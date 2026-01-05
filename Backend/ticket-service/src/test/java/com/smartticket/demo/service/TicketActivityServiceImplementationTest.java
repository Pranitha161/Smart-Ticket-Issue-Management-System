package com.smartticket.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartticket.demo.entity.TicketActivity;
import com.smartticket.demo.enums.ACTION_TYPE;
import com.smartticket.demo.repository.TicketActivityRepository;
import com.smartticket.demo.service.implementation.TicketActivityServiceImplementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class TicketActivityServiceImplementationTest {
	@Mock
	private TicketActivityRepository ticketActivityRepo;
	@InjectMocks
	private TicketActivityServiceImplementation service;

	@Test
	void log_success() {
		TicketActivity activity = TicketActivity.builder().ticketId("T1").actorId("U1").actionType(ACTION_TYPE.CREATED)
				.details("Ticket created").timestamp(Instant.now()).build();
		when(ticketActivityRepo.save(any(TicketActivity.class))).thenReturn(Mono.just(activity));
		StepVerifier.create(service.log("T1", "U1", ACTION_TYPE.CREATED, "Ticket created", Instant.now()))
				.assertNext(saved -> {
					assertEquals("T1", saved.getTicketId());
					assertEquals(ACTION_TYPE.CREATED, saved.getActionType());
				}).verifyComplete();
	}

	@Test
	void log_errorOnSave() {
		when(ticketActivityRepo.save(any(TicketActivity.class)))
				.thenReturn(Mono.error(new RuntimeException("DB error")));
		StepVerifier.create(service.log("T1", "U1", ACTION_TYPE.CREATED, "details", Instant.now()))
				.expectErrorMatches(ex -> ex.getMessage().equals("DB error")).verify();
	}

	@Test
	void getTimeline_success() {
		TicketActivity a1 = TicketActivity.builder().ticketId("T1").actionType(ACTION_TYPE.CREATED)
				.timestamp(Instant.now()).build();
		TicketActivity a2 = TicketActivity.builder().ticketId("T1").actionType(ACTION_TYPE.ASSIGNED)
				.timestamp(Instant.now().plusSeconds(60)).build();
		when(ticketActivityRepo.findByTicketIdOrderByTimestampAsc("T1")).thenReturn(Flux.just(a1, a2));
		StepVerifier.create(service.getTimeline("T1")).expectNext(a1).expectNext(a2).verifyComplete();
	}

	@Test
	void getTimeline_empty() {
		when(ticketActivityRepo.findByTicketIdOrderByTimestampAsc("T1")).thenReturn(Flux.empty());
		StepVerifier.create(service.getTimeline("T1")).verifyComplete();
	}

	@Test
	void getSlaCompliance_success() {
		TicketActivity resolved = TicketActivity.builder().ticketId("T1").actionType(ACTION_TYPE.RESOLVED)
				.timestamp(Instant.now()).build();
		TicketActivity assigned = TicketActivity.builder().ticketId("T1").actionType(ACTION_TYPE.ASSIGNED)
				.timestamp(Instant.now().minusSeconds(600)).build();
		when(ticketActivityRepo.findAll()).thenReturn(Flux.just(resolved));
		when(ticketActivityRepo.findByTicketId("T1")).thenReturn(Flux.just(assigned, resolved));
		StepVerifier.create(service.getSlaCompliance()).assertNext(report -> {
			assertEquals("T1", report.getTicketId());
			assertEquals(10, report.getResolutionMinutes());
		}).verifyComplete();
	}

	@Test
	void getSlaCompliance_noResolvedTickets() {
		TicketActivity assigned = TicketActivity.builder().ticketId("T1").actionType(ACTION_TYPE.ASSIGNED)
				.timestamp(Instant.now()).build();
		when(ticketActivityRepo.findAll()).thenReturn(Flux.just(assigned));
		StepVerifier.create(service.getSlaCompliance()).verifyComplete();
	}

	@Test
	void getSlaCompliance_noAssignedBeforeResolved() {
		TicketActivity resolved = TicketActivity.builder().ticketId("T1").actionType(ACTION_TYPE.RESOLVED)
				.timestamp(Instant.now()).build();
		when(ticketActivityRepo.findAll()).thenReturn(Flux.just(resolved));
		when(ticketActivityRepo.findByTicketId("T1")).thenReturn(Flux.just(resolved));
		StepVerifier.create(service.getSlaCompliance()).verifyComplete();
	}

}
