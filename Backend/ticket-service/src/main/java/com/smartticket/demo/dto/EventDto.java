package com.smartticket.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventDto {
	private String ticketId;
	private String eventType;
	private String ticketStatus;
	private String timestamp;
	private String userId;
}
