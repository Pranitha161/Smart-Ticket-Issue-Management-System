package com.smartticket.demo.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.smartticket.demo.enums.ASSIGNMENT_STATUS;
import com.smartticket.demo.enums.ASSIGNMENT_TYPE;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Document(collection = "assignments")
@Data
@Builder
public class Assignment {
	@Id
	private String id;
	@NotBlank(message = "Ticket ID is required")
	private String ticketId;
	@NotBlank(message = "Agent ID is required")
	private String agentId;
	@NotNull(message = "AssignedAt timestamp is required")
	private Instant assignedAt;
	private Instant dueAt;
	private Instant unassignedAt;
	private boolean breached;
	private Instant breachedAt;
	private ASSIGNMENT_STATUS status;
	private ASSIGNMENT_TYPE type;
	private int escalationLevel;
}
