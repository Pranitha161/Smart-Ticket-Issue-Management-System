package com.smartticket.demo.producer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import com.smartticket.demo.dto.AssignmentEvent;
import com.smartticket.demo.dto.EscalationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class AssignmentEscalationEventProducerTest {

	@Mock
	private KafkaTemplate<String, AssignmentEvent> assignmentKafkaTemplate;

	@Mock
	private KafkaTemplate<String, EscalationEvent> escalationKafkaTemplate;

	private AssignmentEscalationEventProducer producer;

	@BeforeEach
	void setUp() {
		producer = new AssignmentEscalationEventProducer(assignmentKafkaTemplate, escalationKafkaTemplate);
	}

	@Nested
	@DisplayName("Positive Test Cases")
	class PositiveTests {

		@Test
		@DisplayName("Should build and send AssignmentEvent with correct data")
		void testPublishAssignmentEvent_Success() {

			String ticketId = "TKT-100";
			String agentId = "USER-1";
			String action = "REASSIGNED";
			ArgumentCaptor<AssignmentEvent> captor = ArgumentCaptor.forClass(AssignmentEvent.class);

			producer.publishAssignmentEvent(ticketId, agentId, action);

			verify(assignmentKafkaTemplate).send(eq("assignment-events"), captor.capture());
			AssignmentEvent capturedEvent = captor.getValue();

			assertEquals(ticketId, capturedEvent.getTicketId());
			assertEquals(agentId, capturedEvent.getUserId());
			assertEquals(action, capturedEvent.getEventType());
			assertNotNull(capturedEvent.getTimestamp());
		}

		@Test
		@DisplayName("Should build and send EscalationEvent with correct data")
		void testPublishEscalationEvent_Success() {

			String ticketId = "TKT-200";
			String agentId = "MGR-5";
			int level = 3;
			ArgumentCaptor<EscalationEvent> captor = ArgumentCaptor.forClass(EscalationEvent.class);

			producer.publishEscalationEvent(ticketId, agentId, level);

			verify(escalationKafkaTemplate).send(eq("escalation-events"), captor.capture());
			EscalationEvent capturedEvent = captor.getValue();

			assertEquals(ticketId, capturedEvent.getTicketId());
			assertEquals(level, capturedEvent.getEscalationLevel());
			assertEquals("ESCALATED", capturedEvent.getEventType());
		}
	}

	@Nested
	@DisplayName("Negative Test Cases")
	class NegativeTests {

		@Test
		@DisplayName("Should handle Kafka runtime exception gracefully without crashing")
		void testPublishAssignmentEvent_KafkaError() {
			when(assignmentKafkaTemplate.send(any(), any()))
					.thenThrow(new RuntimeException("Kafka Connection Timeout"));
			assertDoesNotThrow(() -> producer.publishAssignmentEvent("id", "user", "act"));
			verify(assignmentKafkaTemplate, times(1)).send(any(), any());
		}

		@Test
		@DisplayName("Should handle null inputs gracefully")
		void testPublishAssignmentEvent_NullInputs() {
			producer.publishAssignmentEvent(null, null, null);
			verify(assignmentKafkaTemplate).send(eq("assignment-events"), any(AssignmentEvent.class));
		}
	}

	@Test
	@DisplayName("Should return a future and execute send asynchronously")
	void testAsyncSendBehavior() {

		CompletableFuture<SendResult<String, AssignmentEvent>> future = new CompletableFuture<>();
		when(assignmentKafkaTemplate.send(anyString(), any())).thenReturn(future);
		producer.publishAssignmentEvent("T-1", "A-1", "CREATE");
		verify(assignmentKafkaTemplate).send(eq("assignment-events"), any());
	}

	@Test
	@DisplayName("Should ensure timestamp is valid ISO-8601 format")
	void testEventTimestampFormat() {
		ArgumentCaptor<AssignmentEvent> captor = ArgumentCaptor.forClass(AssignmentEvent.class);
		producer.publishAssignmentEvent("ID", "USER", "ACT");
		verify(assignmentKafkaTemplate).send(anyString(), captor.capture());
		String timestamp = captor.getValue().getTimestamp();
		assertDoesNotThrow(() -> Instant.parse(timestamp));
	}
}