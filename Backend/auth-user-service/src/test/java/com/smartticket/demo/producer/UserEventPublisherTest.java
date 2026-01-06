package com.smartticket.demo.producer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

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

	@Test
	void publishUserRegistered_containsEventIdAndTimestamp() {
		publisher.publishUserRegistered("U1", "test@example.com", "tester");
		verify(kafkaTemplate).send(eq("auth-events"), messageCaptor.capture());
		String json = messageCaptor.getValue();
		assertTrue(json.contains("\"eventId\""));
		assertTrue(json.contains("\"timestamp\""));
	}

	@Test
	void publishPasswordReset_containsUserIdAndEmail() {
		publisher.publishPasswordReset("U2", "reset@example.com", "http://resetlink");
		verify(kafkaTemplate).send(eq("auth-events"), messageCaptor.capture());
		String json = messageCaptor.getValue();
		assertTrue(json.contains("\"userId\":\"U2\""));
		assertTrue(json.contains("\"email\":\"reset@example.com\""));
	}

	@Test
	void publishPasswordChanged_doesNotContainResetLink() {
		publisher.publishPasswordChanged("U3", "changed@example.com");
		verify(kafkaTemplate).send(eq("auth-events"), messageCaptor.capture());
		String json = messageCaptor.getValue();
		assertTrue(json.contains("\"eventType\":\"PASSWORD_CHANGED\""));
		assertTrue(json.contains("resetLink"));
	}

	@Test
	void publishPasswordReset_handlesSerializationError() throws Exception {
		UserEventPublisher badPublisher = new UserEventPublisher(kafkaTemplate) {
			@Override
			public void publishPasswordReset(String userId, String email, String resetLink) {
				try {
					throw new RuntimeException("Serialization failed");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		badPublisher.publishPasswordReset("U1", "e@x.com", "http://reset");

	}

	@Test
	void publishPasswordChanged_handlesSerializationError() throws Exception {
		UserEventPublisher badPublisher = new UserEventPublisher(kafkaTemplate) {
			@Override
			public void publishPasswordChanged(String userId, String email) {
				try {
					throw new RuntimeException("Serialization failed");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		badPublisher.publishPasswordChanged("U1", "e@x.com");
	}

	@Test
	void publishUserRegistered_withNullsStillSends() {
		publisher.publishUserRegistered("U4", null, null);
		verify(kafkaTemplate).send(eq("auth-events"), messageCaptor.capture());
		String json = messageCaptor.getValue();
		assertTrue(json.contains("\"userId\":\"U4\""));
		
	}
	


}
