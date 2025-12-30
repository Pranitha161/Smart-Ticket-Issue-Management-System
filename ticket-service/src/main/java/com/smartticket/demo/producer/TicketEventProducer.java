package com.smartticket.demo.producer;

import java.time.Instant;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.smartticket.demo.dto.EventDto;
import com.smartticket.demo.entity.Ticket;

@Service
public class TicketEventProducer {
	
	 private final KafkaTemplate<String, EventDto> kafkaTemplate;

	    public TicketEventProducer(KafkaTemplate<String, EventDto> kafkaTemplate) {
	        this.kafkaTemplate = kafkaTemplate;
	    }

	public void publishTicketEvent(Ticket ticket, String action) {
		EventDto event = EventDto.builder() .ticketId(ticket.getDisplayId()) 
				.eventType(action) 
				.ticketStatus(ticket.getStatus().name())
				.timestamp(Instant.now().toString()) 
				.userId(ticket.getCreatedBy())
				.build();
		 try {
	            kafkaTemplate.send("ticket-events", event);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		
	}
}
