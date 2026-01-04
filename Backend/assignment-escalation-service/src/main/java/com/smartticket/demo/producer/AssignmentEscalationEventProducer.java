package com.smartticket.demo.producer;

import java.time.Instant;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.smartticket.demo.dto.AssignmentEvent;
import com.smartticket.demo.dto.EscalationEvent;

@Service
public class AssignmentEscalationEventProducer {

    private final KafkaTemplate<String, AssignmentEvent> assignmentKafkaTemplate;
    private final KafkaTemplate<String, EscalationEvent> escalationKafkaTemplate;

    public AssignmentEscalationEventProducer(KafkaTemplate<String, AssignmentEvent> assignmentKafkaTemplate,
                                             KafkaTemplate<String, EscalationEvent> escalationKafkaTemplate) {
        this.assignmentKafkaTemplate = assignmentKafkaTemplate;
        this.escalationKafkaTemplate = escalationKafkaTemplate;
    }

    public void publishAssignmentEvent(String ticketId, String agentId, String action) {
        AssignmentEvent event = AssignmentEvent.builder()
                .ticketId(ticketId)
                .userId(agentId)
                .eventType(action)
                .timestamp(Instant.now().toString())
                .build();
        try {
            assignmentKafkaTemplate.send("assignment-events", event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publishEscalationEvent(String ticketId, String agentId, int level) {
        EscalationEvent event = EscalationEvent.builder()
                .ticketId(ticketId)
                .userId(agentId)
                .eventType("ESCALATED")
                .escalationLevel(level)
                .timestamp(Instant.now().toString())
                .build();
        try {
            escalationKafkaTemplate.send("escalation-events", event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
