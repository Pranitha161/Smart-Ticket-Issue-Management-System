package com.smartticket.demo.producer;

import com.smartticket.demo.dto.EventDto;
import com.smartticket.demo.entity.Ticket;
import com.smartticket.demo.enums.STATUS;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TicketEventProducerTest {

	private KafkaTemplate<String, EventDto> kafkaTemplate;
	private TicketEventProducer producer;

	@BeforeEach
	void setup() {
		kafkaTemplate = mock(KafkaTemplate.class);
		producer = new TicketEventProducer(kafkaTemplate);
	}

	@Test
	void publishTicketEvent_sendsCorrectEvent() {
		Ticket ticket = new Ticket();
		ticket.setDisplayId("T1");
		ticket.setCreatedBy("U1");
		ticket.setStatus(STATUS.OPEN); 

		ArgumentCaptor<EventDto> captor = ArgumentCaptor.forClass(EventDto.class);

		producer.publishTicketEvent(ticket, "CREATED");

		verify(kafkaTemplate).send(eq("ticket-events"), captor.capture());
		EventDto event = captor.getValue();

		assertEquals("T1", event.getTicketId());
		assertEquals("CREATED", event.getEventType());
		assertEquals("OPEN", event.getTicketStatus());
		assertEquals("U1", event.getUserId());
		assertNotNull(event.getTimestamp());
	}

	@Test
	void publishTicketEvent_handlesSendException() {
		Ticket ticket = new Ticket();
		ticket.setDisplayId("T2");
		ticket.setCreatedBy("U2");
		ticket.setStatus(STATUS.CLOSED);

		doThrow(new RuntimeException("Kafka send failed")).when(kafkaTemplate).send(eq("ticket-events"),
				any(EventDto.class));

		producer.publishTicketEvent(ticket, "UPDATED");

		verify(kafkaTemplate).send(eq("ticket-events"), any(EventDto.class));
	}
}
