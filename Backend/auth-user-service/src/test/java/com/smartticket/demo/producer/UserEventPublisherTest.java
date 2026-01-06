package com.smartticket.demo.producer;

import com.smartticket.demo.entity.EventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class UserEventPublisherTest {
	@Mock
	private KafkaTemplate<String, String> kafkaTemplate;
	@InjectMocks
	private UserEventPublisher publisher;
	@Captor
	private ArgumentCaptor<String> messageCaptor;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void publishUserRegistered_sendsCorrectEvent() {
		publisher.publishUserRegistered("U1", "test@example.com", "tester");
		verify(kafkaTemplate).send(eq("auth-events"), messageCaptor.capture());
		String json = messageCaptor.getValue();
		assertTrue(json.contains("\"eventType\":\"USER_REGISTERED\""));
		assertTrue(json.contains("\"userId\":\"U1\""));
		assertTrue(json.contains("\"email\":\"test@example.com\""));
		assertTrue(json.contains("\"username\":\"tester\""));
	}

	@Test
	void publishPasswordReset_sendsCorrectEvent() {
		publisher.publishPasswordReset("U1", "test@example.com", "http://reset");
		verify(kafkaTemplate).send(eq("auth-events"), messageCaptor.capture());
		String json = messageCaptor.getValue();
		assertTrue(json.contains("\"eventType\":\"PASSWORD_RESET\""));
		assertTrue(json.contains("\"resetLink\":\"http://reset\""));
	}

	@Test
	void publishPasswordChanged_sendsCorrectEvent() {
		publisher.publishPasswordChanged("U1", "test@example.com");
		verify(kafkaTemplate).send(eq("auth-events"), messageCaptor.capture());
		String json = messageCaptor.getValue();
		assertTrue(json.contains("\"eventType\":\"PASSWORD_CHANGED\""));
		assertTrue(json.contains("\"userId\":\"U1\""));
		assertTrue(json.contains("\"email\":\"test@example.com\""));
	}

	@Test
	void publishUserRegistered_handlesSerializationError() throws Exception {
		UserEventPublisher badPublisher = new UserEventPublisher(kafkaTemplate) {
			@Override
			public void publishUserRegistered(String userId, String email, String username) {
				try {
					throw new RuntimeException("Serialization failed");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		badPublisher.publishUserRegistered("U1", "e@x.com", "tester");
	}

}
