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

public class NotificationConsumerTest {

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
}
