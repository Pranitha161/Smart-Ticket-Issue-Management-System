package com.smartticket.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentEvent {
	private String ticketId;
	private String eventType;
	private String userId; 
	private String timestamp;
}
