package com.smartticket.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.smartticket.demo.enums.PRIORITY;
import com.smartticket.demo.enums.STATUS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "tickets")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Ticket {
	@Id 
	private String ticketId;
	private String title;
	private String description; 
	private STATUS status; 
	private PRIORITY priority;						
	private String createdBy; 						
	private String assignedTo; 
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
