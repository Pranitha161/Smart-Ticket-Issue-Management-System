package com.smartticket.demo.controller;

import com.smartticket.demo.entity.Notification;
import com.smartticket.demo.service.NotificationService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

	@Mock
	private NotificationService notificationService;
	@InjectMocks
	private NotificationController controller;

	@Test
	void sendNotification_success() {
		Notification n = Notification.builder().id("N1").recipient("user@example.com").subject("Hello")
				.body("Test body").build();

		when(notificationService.sendEmail(n)).thenReturn(Mono.just(n));

		StepVerifier.create(controller.sendNotification(n)).assertNext(result -> {
			assertEquals("N1", result.getId());
			assertEquals("user@example.com", result.getRecipient());
		}).verifyComplete();
	}

	@Test
	void sendNotification_error() {
		Notification n = Notification.builder().id("N1").recipient("user@example.com").build();
		when(notificationService.sendEmail(n)).thenReturn(Mono.error(new RuntimeException("Mail error")));

		StepVerifier.create(controller.sendNotification(n))
				.expectErrorMatches(ex -> ex.getMessage().equals("Mail error")).verify();
	}

	@Test
	void getStatus_success() {
		Notification n = Notification.builder().id("N1").recipient("user@example.com").build();
		when(notificationService.notificationsById("N1")).thenReturn(Mono.just(n));

		StepVerifier.create(controller.getStatus("N1")).assertNext(result -> assertEquals("N1", result.getId()))
				.verifyComplete();
	}

	@Test
	void getStatus_notFound() {
		when(notificationService.notificationsById("N1")).thenReturn(Mono.empty());

		StepVerifier.create(controller.getStatus("N1")).verifyComplete();
	}

	@Test
	void getHistory_success() {
		Notification n1 = Notification.builder().id("N1").recipient("user@example.com").build();
		Notification n2 = Notification.builder().id("N2").recipient("user@example.com").build();

		when(notificationService.notificationsByEmail("user@example.com"))
				.thenReturn(Flux.fromIterable(List.of(n1, n2)));

		StepVerifier.create(controller.getHistory("user@example.com")).expectNext(n1).expectNext(n2).verifyComplete();
	}

	@Test
	void getHistory_empty() {
		when(notificationService.notificationsByEmail("user@example.com")).thenReturn(Flux.empty());
		StepVerifier.create(controller.getHistory("user@example.com")).verifyComplete();
	}
}
