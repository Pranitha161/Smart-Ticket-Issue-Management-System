package com.smartticket.demo.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartticket.demo.entity.EventDTO;
import com.smartticket.demo.entity.Notification;
import com.smartticket.demo.feign.UserClient;
import com.smartticket.demo.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class NotificationConsumerTest {

	@Mock
	private NotificationService notificationService;

	@Mock
	private UserClient userClient;

	@InjectMocks
	private NotificationConsumer consumer;

	private ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		when(notificationService.sendEmail(any(Notification.class))).thenReturn(reactor.core.publisher.Mono.empty());
	}

	@Test
	void consume_userRegisteredEvent_success() throws Exception {
		EventDTO event = new EventDTO();
		event.setEventType("USER_REGISTERED");
		event.setUserId("U1");
		event.setUsername("Pranitha");
		event.setEmail("test@example.com");

		String message = objectMapper.writeValueAsString(event);

		consumer.consume(message);

		verify(notificationService, times(1))
				.sendEmail(argThat(notification -> notification.getSubject().contains("Welcome")
						&& notification.getBody().contains("Pranitha")));
	}

	@Test
	void consume_passwordResetEvent_success() throws Exception {
		EventDTO event = new EventDTO();
		event.setEventType("PASSWORD_RESET");
		event.setUserId("U1");
		event.setResetLink("http://reset-link");
		event.setEmail("test@example.com");

		String message = objectMapper.writeValueAsString(event);

		consumer.consume(message);

		verify(notificationService)
				.sendEmail(argThat(notification -> notification.getSubject().contains("Password Reset")
						&& notification.getBody().contains("http://reset-link")));
	}

	@Test
	void consume_ticketAssignedEvent_withMissingEmail_fetchesFromUserClient() throws Exception {
		EventDTO event = new EventDTO();
		event.setEventType("ASSIGNED");
		event.setUserId("U1");
		event.setTicketId("T123");
		event.setEmail(null);

		when(userClient.getUserEmail("U1")).thenReturn("fetched@example.com");

		String message = objectMapper.writeValueAsString(event);

		consumer.consume(message);

		verify(notificationService)
				.sendEmail(argThat(notification -> notification.getRecipient().equals("fetched@example.com")
						&& notification.getSubject().contains("Assigned")));
	}

	@Test
	void consume_unknownEventType_defaultsToGenericNotification() throws Exception {
		EventDTO event = new EventDTO();
		event.setEventType("SOME_UNKNOWN_EVENT");
		event.setUserId("U1");
		event.setEmail("test@example.com");

		String message = objectMapper.writeValueAsString(event);

		consumer.consume(message);

		verify(notificationService).sendEmail(argThat(notification -> notification.getSubject().equals("Notification")
				&& notification.getBody().contains("SOME_UNKNOWN_EVENT")));
	}

	@Test
	void consume_invalidJson_doesNotThrow() {
		consumer.consume("not-a-json");
		verify(notificationService, never()).sendEmail(any());
	}

	@Test
	void consume_passwordChangedEvent_success() throws Exception {
		EventDTO event = new EventDTO();
		event.setEventType("PASSWORD_CHANGED");
		event.setUserId("U1");
		event.setEmail("test@example.com");
		String message = objectMapper.writeValueAsString(event);
		consumer.consume(message);
		verify(notificationService)
				.sendEmail(argThat(notification -> notification.getSubject().contains("Password Changed")
						&& notification.getBody().contains("updated")));
	}

	@Test
	void consume_ticketCreatedEvent_success() throws Exception {
		EventDTO event = new EventDTO();
		event.setEventType("CREATED");
		event.setTicketId("T123");
		event.setEmail("test@example.com");
		String message = objectMapper.writeValueAsString(event);
		consumer.consume(message);
		verify(notificationService).sendEmail(argThat(notification -> notification.getSubject().contains("Created")
				&& notification.getBody().contains("created")));
	}

	@Test
	void consume_ticketUpdatedEvent_success() throws Exception {
		EventDTO event = new EventDTO();
		event.setEventType("UPDATED");
		event.setTicketId("T123");
		event.setTicketStatus("IN_PROGRESS");
		event.setEmail("test@example.com");
		String message = objectMapper.writeValueAsString(event);
		consumer.consume(message);
		verify(notificationService).sendEmail(argThat(notification -> notification.getSubject().contains("Updated")
				&& notification.getBody().contains("IN_PROGRESS")));
	}

	@Test
	void consume_ticketDeletedEvent_success() throws Exception {
		EventDTO event = new EventDTO();
		event.setEventType("DELETED");
		event.setTicketId("T123");
		event.setEmail("test@example.com");
		String message = objectMapper.writeValueAsString(event);
		consumer.consume(message);
		verify(notificationService).sendEmail(argThat(notification -> notification.getSubject().contains("Deleted")
				&& notification.getBody().contains("deleted")));
	}

	@Test
	void consume_ticketReassignedEvent_success() throws Exception {
		EventDTO event = new EventDTO();
		event.setEventType("REASSIGNED");
		event.setTicketId("T123");
		event.setAssignedTo("AgentX");
		event.setEmail("test@example.com");
		String message = objectMapper.writeValueAsString(event);
		consumer.consume(message);
		verify(notificationService).sendEmail(argThat(notification -> notification.getSubject().contains("Reassigned")
				&& notification.getBody().contains("AgentX")));
	}

	@Test
	void consume_ticketEscalatedEvent_success() throws Exception {
		EventDTO event = new EventDTO();
		event.setEventType("ESCALATED");
		event.setTicketId("T123");
		event.setEscalationLevel("L2");
		event.setEmail("test@example.com");
		String message = objectMapper.writeValueAsString(event);
		consumer.consume(message);
		verify(notificationService).sendEmail(argThat(notification -> notification.getSubject().contains("Escalated")
				&& notification.getBody().contains("L2")));
	}

	@Test
	void consume_ticketSlaBreachedEvent_success() throws Exception {
		EventDTO event = new EventDTO();
		event.setEventType("SLA_BREACHED");
		event.setTicketId("T123");
		event.setEmail("test@example.com");
		String message = objectMapper.writeValueAsString(event);
		consumer.consume(message);
		verify(notificationService).sendEmail(argThat(notification -> notification.getSubject().contains("SLA Breach")
				&& notification.getBody().contains("breached SLA")));
	}

	@Test
	void consume_eventWithBlankEmail_fetchesFromUserClient() throws Exception {
		EventDTO event = new EventDTO();
		event.setEventType("CREATED");
		event.setTicketId("T123");
		event.setEmail(" ");
		when(userClient.getUserEmail("U1")).thenReturn("fallback@example.com");
		event.setUserId("U1");
		String message = objectMapper.writeValueAsString(event);
		consumer.consume(message);
		verify(notificationService)
				.sendEmail(argThat(notification -> notification.getRecipient().equals("fallback@example.com")));
	}

	@Test
	void consume_eventWithNullTicketId_stillProcesses() throws Exception {
		EventDTO event = new EventDTO();
		event.setEventType("CREATED");
		event.setTicketId(null);
		event.setEmail("test@example.com");
		String message = objectMapper.writeValueAsString(event);
		consumer.consume(message);
		verify(notificationService)
				.sendEmail(argThat(notification -> notification.getSubject().contains("Ticket #null")));
	}

	@Test
	void consume_eventThrowsException_doesNotPropagate() throws Exception {
		consumer.consume("{\"eventType\": \"CREATED\", \"ticketId\": }");
		verify(notificationService, never()).sendEmail(any());
	}
}
