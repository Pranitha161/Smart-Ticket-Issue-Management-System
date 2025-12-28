package com.smartticket.demo.producer;

import java.time.Instant;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartticket.demo.entity.EventDTO;

@Service
public class UserEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserRegistered(String userId, String email, String username) {
        EventDTO event = new EventDTO();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("USER_REGISTERED");
        event.setTimestamp(Instant.now().toString());
        event.setUserId(userId);
        event.setEmail(email);
        event.setUsername(username);

        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("auth-events", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publishPasswordReset(String userId, String email, String resetLink) {
        EventDTO event = new EventDTO();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("PASSWORD_RESET");
        event.setTimestamp(Instant.now().toString());
        event.setUserId(userId);
        event.setEmail(email);
        event.setResetLink(resetLink);

        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("auth-events", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publishPasswordChanged(String userId, String email) {
        EventDTO event = new EventDTO();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("PASSWORD_CHANGED");
        event.setTimestamp(Instant.now().toString());
        event.setUserId(userId);
        event.setEmail(email);

        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("auth-events", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

