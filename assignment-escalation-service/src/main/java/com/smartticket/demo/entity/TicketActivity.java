package com.smartticket.demo.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;

import com.smartticket.demo.enums.ACTION_TYPE;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketActivity {
	@Id
	private String id;
	private String ticketId;
	private String actorId;
	private ACTION_TYPE actionType;
	private String details;
	private Instant timestamp;
}
