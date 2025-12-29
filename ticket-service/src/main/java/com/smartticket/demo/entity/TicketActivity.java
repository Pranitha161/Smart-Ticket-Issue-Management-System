package com.smartticket.demo.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.smartticket.demo.enums.ACTION_TYPE;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Document(collection = "ticket_activity")
@Data
@RequiredArgsConstructor
public class TicketActivity {
	@Id
	private String id;
	private String ticketId;
	private String actorId;
	private ACTION_TYPE actionType;
	private String details;
	private Instant timestamp;
}