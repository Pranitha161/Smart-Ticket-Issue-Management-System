package com.smartticket.demo.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartticket.demo.entity.EventDTO;
import com.smartticket.demo.entity.Notification;
import com.smartticket.demo.feign.UserClient;
import com.smartticket.demo.service.NotificationService;

@Component
public class NotificationConsumer {

	private final NotificationService notificationService;
	private final UserClient userClient;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public NotificationConsumer(NotificationService notificationService,UserClient userClient) {
		this.notificationService = notificationService;
		this.userClient=userClient;
	}

	@KafkaListener(topics = { "ticket-events", "auth-events", "assignment-events" }, groupId = "notification-service")
	public void consume(String message) {
		try {
			EventDTO payload = objectMapper.readValue(message, EventDTO.class);
			Notification notification = new Notification();
			notification.setEventType(payload.getEventType());
			notification.setCreatedAt(java.time.Instant.now());
			String recipient = payload.getEmail();
			if (recipient == null || recipient.isBlank()) { 
				recipient = userClient.getUserEmail(payload.getUserId()); 
				}
			notification.setRecipient(recipient);
			

			switch (payload.getEventType()) {
			case "USER_REGISTERED":
				notification.setSubject("Welcome to Smart Ticket System");
				notification
						.setBody("Hello " + payload.getUsername() + ", your account has been created successfully.");
				break;

			case "PASSWORD_RESET":
				notification.setSubject("Password Reset Request");
				notification.setBody("Click here to reset your password: " + payload.getResetLink());
				break;

			case "PASSWORD_CHANGED":
				notification.setSubject("Password Changed Successfully");
				notification.setBody("Your password has been updated. If this wasn’t you, contact support.");
				break;

			case "CREATED":
				notification.setSubject("Ticket #" + payload.getTicketId() + " Created");
				notification.setBody("Your ticket has been created");
				break;

			case "UPDATED":
				notification.setSubject("Ticket #" + payload.getTicketId() + " Updated");
				notification.setBody("Your ticket status is now: " + payload.getTicketStatus());
				break;

			case "DELETED":
				notification.setSubject("Ticket #" + payload.getTicketId() + " Deleted");
				notification.setBody("Your ticket has been deleted as per your request.");
				break;

			case "ASSIGNED":
				notification.setSubject("Ticket #" + payload.getTicketId() + " Assigned");
				notification.setBody("You have been assigned ticket #" + payload.getTicketId() + ".");
				break;

			case "REASSIGNED":
				notification.setSubject("Ticket #" + payload.getTicketId() + " Reassigned");
				notification.setBody("Ticket #" + payload.getTicketId() + " has been reassigned to "
						+ payload.getAssignedTo() + ".");
				break;
			case "ESCALATED":
				notification.setSubject("Ticket #" + payload.getTicketId() + " Escalated");
				notification.setBody("Ticket #" + payload.getTicketId() + " has been escalated to "
						+ payload.getEscalationLevel() + ".");
				break;
			case "SLA_BREACHED":
				notification.setSubject("Ticket #" + payload.getTicketId() + " SLA Breach");
				notification.setBody(
						"Ticket #" + payload.getTicketId() + " has breached SLA and requires immediate attention.");
				break;

			default:
				notification.setSubject("Notification");
				notification.setBody("Event received: " + payload.getEventType());
			}

			notificationService.sendEmail(notification).subscribe();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
