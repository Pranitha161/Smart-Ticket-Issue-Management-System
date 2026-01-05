package com.smartticket.demo.service;

import com.smartticket.demo.entity.Notification;
import com.smartticket.demo.repository.NotificationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private JavaMailSender mailSender;
	@Mock
	private NotificationRepository notificationRepo;

	@InjectMocks
	private NotificationService service;

	@Test
	void sendEmail_success() {
		Notification notification = Notification.builder().id("N1").recipient("user@example.com").subject("Hello")
				.body("Test body").build();

		when(notificationRepo.save(notification)).thenReturn(Mono.just(notification));

		StepVerifier.create(service.sendEmail(notification)).assertNext(saved -> {
			assertEquals("N1", saved.getId());
			assertEquals("user@example.com", saved.getRecipient());
		}).verifyComplete();
		verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
	}

	@Test
	void sendEmail_errorOnSave() {
		Notification notification = Notification.builder().id("N1").recipient("user@example.com").subject("Hello")
				.body("Test body").build();

		when(notificationRepo.save(notification)).thenReturn(Mono.error(new RuntimeException("DB error")));

		StepVerifier.create(service.sendEmail(notification))
				.expectErrorMatches(ex -> ex.getMessage().equals("DB error")).verify();

		verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
	}

	@Test
	void notificationsById_success() {
		Notification notification = Notification.builder().id("N1").recipient("user@example.com").build();
		when(notificationRepo.findById("N1")).thenReturn(Mono.just(notification));

		StepVerifier.create(service.notificationsById("N1")).assertNext(result -> assertEquals("N1", result.getId()))
				.verifyComplete();
	}

	@Test
	void notificationsById_notFound() {
		when(notificationRepo.findById("N1")).thenReturn(Mono.empty());

		StepVerifier.create(service.notificationsById("N1")).verifyComplete();
	}

	@Test
	void notificationsByEmail_success() {
		Notification n1 = Notification.builder().id("N1").recipient("user@example.com").build();
		Notification n2 = Notification.builder().id("N2").recipient("other@example.com").build();

		when(notificationRepo.findAll()).thenReturn(Flux.fromIterable(List.of(n1, n2)));

		StepVerifier.create(service.notificationsByEmail("user@example.com"))
				.assertNext(result -> assertEquals("user@example.com", result.getRecipient())).verifyComplete();
	}

	@Test
	void notificationsByEmail_noMatch() {
		Notification n1 = Notification.builder().id("N1").recipient("other@example.com").build();
		when(notificationRepo.findAll()).thenReturn(Flux.just(n1));

		StepVerifier.create(service.notificationsByEmail("user@example.com")).verifyComplete();
	}
}
