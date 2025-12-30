package com.smartticket.demo.producer;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.smartticket.demo.dto.AssignmentEvent;
import com.smartticket.demo.dto.EscalationEvent;

@Service
public class AssignmentEscalationEventProducer {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	public void publishAssignmentEvent(String ticketId, String agentId, String action) {
		AssignmentEvent event = AssignmentEvent.builder().ticketId(ticketId).action(action).agentId(agentId)
				.timestamp(Instant.now()).build();

		kafkaTemplate.send("assignment-events", event);
	}

	public void publishEscalationEvent(String ticketId, String agentId, int level) {
		EscalationEvent event = EscalationEvent.builder().ticketId(ticketId).action("ESCALATED").agentId(agentId)
				.escalationLevel(level).timestamp(Instant.now()).build();

		kafkaTemplate.send("escalation-events", event);
	}
}
