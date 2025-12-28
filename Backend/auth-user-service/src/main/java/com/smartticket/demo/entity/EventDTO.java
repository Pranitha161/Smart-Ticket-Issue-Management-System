package com.smartticket.demo.entity;

import lombok.Data;

@Data
public class EventDTO {
    private String eventId;
    private String eventType;
    private String timestamp;

    private String userId;
    private String email;
    private String username;
    private String resetLink;
    private String message;

}
