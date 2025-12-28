package com.smartticket.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "assignments")
@Data
public class Assignment {
	private String id;
	private String ticketId;
	private String agentId;
	private LocalDateTime assignedAt;
	private LocalDateTime dueAt;
	private LocalDateTime unassignedAt;
	private boolean breached;
	private LocalDateTime breachedAt;
	private String escalationLevel;
}
