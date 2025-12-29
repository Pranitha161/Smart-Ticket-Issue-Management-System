package com.smartticket.demo.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketEvent {
	private String ticketId;
	private String actorId;
	private String eventType;
	private String details;
	private Instant timestamp;
}
