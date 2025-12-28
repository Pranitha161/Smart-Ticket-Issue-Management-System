package com.smartticket.demo.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartticket.demo.entity.EventDTO;
import com.smartticket.demo.entity.Notification;
import com.smartticket.demo.service.NotificationService;

@Component
public class NotificationConsumer {

	private final NotificationService notificationService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public NotificationConsumer(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@KafkaListener(topics = { "ticket-events", "auth-events", "assignment-events" }, groupId = "notification-service")
	public void consume(String message) {
	    try {
	        EventDTO payload = objectMapper.readValue(message, EventDTO.class);

	        Notification notification = new Notification();
	        notification.setRecipient(payload.getEmail());
	        notification.setEventType(payload.getEventType());
	        notification.setCreatedAt(java.time.Instant.now());

	        switch (payload.getEventType()) {
	            case "USER_REGISTERED":
	                notification.setSubject("Welcome to Smart Ticket System");
	                notification.setBody("Hello " + payload.getUsername() +
	                        ", your account has been created successfully.");
	                break;

	            case "PASSWORD_RESET":
	                notification.setSubject("Password Reset Request");
	                notification.setBody("Click here to reset your password: " + payload.getResetLink());
	                break;

	            case "PASSWORD_CHANGED":
	                notification.setSubject("Password Changed Successfully");
	                notification.setBody("Your password has been updated. If this wasn’t you, contact support.");
	                break;

	            case "TICKET_CREATED":
	                notification.setSubject("Ticket #" + payload.getTicketId() + " Created");
	                notification.setBody("Your ticket has been created and assigned to " + payload.getAssignedTo() + ".");
	                break;

	            case "TICKET_UPDATED":
	                notification.setSubject("Ticket #" + payload.getTicketId() + " Updated");
	                notification.setBody("Your ticket status is now: " + payload.getTicketStatus());
	                break;

	            case "TICKET_DELETED":
	                notification.setSubject("Ticket #" + payload.getTicketId() + " Deleted");
	                notification.setBody("Your ticket has been deleted as per your request.");
	                break;

	            case "TICKET_ASSIGNED":
	                notification.setSubject("Ticket #" + payload.getTicketId() + " Assigned");
	                notification.setBody("You have been assigned ticket #" + payload.getTicketId() + ".");
	                break;

	            case "TICKET_REASSIGNED":
	                notification.setSubject("Ticket #" + payload.getTicketId() + " Reassigned");
	                notification.setBody("Ticket #" + payload.getTicketId() +
	                        " has been reassigned to " + payload.getAssignedTo() + ".");
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
