package com.smartticket.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.smartticket.demo.entity.Notification;
import com.smartticket.demo.repository.NotificationRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class NotificationService {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private NotificationRepository notificationRepo;

	public Mono<Notification> sendEmail(Notification notification) {

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(notification.getRecipient());
		message.setSubject(notification.getSubject());
		message.setText(notification.getBody());
		mailSender.send(message);
		return notificationRepo.save(notification);
	}

	public Mono<Notification> notificationsById(String id) {
		return notificationRepo.findById(id);
	}

	public Flux<Notification> notificationsByEmail(String email) {
		return notificationRepo.findAll().filter(n -> n.getRecipient().equalsIgnoreCase(email));
	}
}
