package com.smartticket.demo.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketEvent {
	private String ticketId;
	private String userId;
	private String eventType;
	private String details;
	private Instant timestamp;
}
