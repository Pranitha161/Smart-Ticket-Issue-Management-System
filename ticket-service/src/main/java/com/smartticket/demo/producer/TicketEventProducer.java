package com.smartticket.demo.producer;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.smartticket.demo.dto.TicketEvent;
import com.smartticket.demo.entity.Ticket;

@Service
public class TicketEventProducer {
	@Autowired
	private KafkaTemplate<String, TicketEvent> kafkaTemplate;

	public void publishTicketEvent(Ticket ticket, String action) {
		TicketEvent event = TicketEvent.builder() .ticketId(ticket.getId()) 
				.eventType(action) 
				.details(ticket.getStatus().name())
				.timestamp(Instant.now()) 
				.build();
		kafkaTemplate.send("ticket-events", event);
		
	}
}
