package com.smartticket.demo.controller;

import com.smartticket.demo.dto.SlaReportDto;
import com.smartticket.demo.entity.TicketActivity;
import com.smartticket.demo.enums.ACTION_TYPE;
import com.smartticket.demo.service.implementation.TicketActivityServiceImplementation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Instant;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)

public class TicketActivityControllerTest {
	@Mock
	private TicketActivityServiceImplementation service;
	@InjectMocks
	private TicketActivityController controller;

	@Test
	void getTimeline_success() {
		TicketActivity a1 = TicketActivity.builder().ticketId("T1").actorId("U1").actionType(ACTION_TYPE.CREATED)
				.timestamp(Instant.now()).build();
		TicketActivity a2 = TicketActivity.builder().ticketId("T1").actorId("U2").actionType(ACTION_TYPE.COMMENT)
				.timestamp(Instant.now()).build();
		when(service.getTimeline("T1")).thenReturn(Flux.just(a1, a2));
		StepVerifier.create(controller.getTimeline("T1")).expectNext(a1).expectNext(a2).verifyComplete();
	}

	@Test
	void getTimeline_empty() {
		when(service.getTimeline("T1")).thenReturn(Flux.empty());
		StepVerifier.create(controller.getTimeline("T1")).verifyComplete();
	}

	@Test
	void logActivity_success() {
		TicketActivity activity = TicketActivity.builder().ticketId("T1").actorId("U1").actionType(ACTION_TYPE.COMMENT)
				.details("Nice work").timestamp(Instant.now()).build();
		when(service.log("T1", "U1", ACTION_TYPE.COMMENT, "Nice work", null)).thenReturn(Mono.just(activity));
		StepVerifier.create(controller.logActivity("T1", "U1", "Nice work")).assertNext(result -> {
			assertEquals("T1", result.getTicketId());
			assertEquals("U1", result.getActorId());
			assertEquals(ACTION_TYPE.COMMENT, result.getActionType());
			assertEquals("Nice work", result.getDetails());
		}).verifyComplete();
	}

	@Test
	void logActivity_error() {
		when(service.log("T1", "U1", ACTION_TYPE.COMMENT, "Bad", null))
				.thenReturn(Mono.error(new RuntimeException("DB error")));
		StepVerifier.create(controller.logActivity("T1", "U1", "Bad"))
				.expectErrorMatches(ex -> ex.getMessage().equals("DB error")).verify();
	}

	@Test
	void getSlaReport_success() {
		SlaReportDto report = SlaReportDto.builder().ticketId("T1").resolutionMinutes(15L).build();
		when(service.getSlaCompliance()).thenReturn(Flux.just(report));
		StepVerifier.create(controller.getSlaReport()).assertNext(r -> {
			assertEquals("T1", r.getTicketId());
			assertEquals(15L, r.getResolutionMinutes());
		}).verifyComplete();
	}

	@Test
	void getSlaReport_empty() {
		when(service.getSlaCompliance()).thenReturn(Flux.empty());
		StepVerifier.create(controller.getSlaReport()).verifyComplete();
	}

}
