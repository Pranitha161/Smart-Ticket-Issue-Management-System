package com.smartticket.demo.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.smartticket.demo.dto.TicketEvent;
import com.smartticket.demo.enums.ACTION_TYPE;
import com.smartticket.demo.service.implementation.TicketActivityServiceImplementation;

@Component
public class TicketEventListener {

	private final TicketActivityServiceImplementation activityService;

	public TicketEventListener(TicketActivityServiceImplementation activityService) {
		this.activityService = activityService;
	}

	@KafkaListener(topics = "ticket-events", groupId = "ticket-service")
	public void onEvent(TicketEvent event) {
		ACTION_TYPE type = map(event.getEventType());
		activityService.log(event.getTicketId(), event.getActorId(), type, event.getDetails(), event.getTimestamp())
				.block();
	}
	
	private ACTION_TYPE map(String eventType) {
		return switch (eventType) {
		case "ASSIGNMENT" -> ACTION_TYPE.ASSIGNMENT;
		case "ESCALATION" -> ACTION_TYPE.ESCALATION;
		case "STATUS_CHANGE" -> ACTION_TYPE.STATUS_CHANGE;
		case "COMMENT" -> ACTION_TYPE.COMMENT;
		default -> ACTION_TYPE.STATUS_CHANGE;
		};
	}
}
