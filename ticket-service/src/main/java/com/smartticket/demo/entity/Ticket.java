package com.smartticket.demo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.smartticket.demo.enums.PRIORITY;
import com.smartticket.demo.enums.STATUS;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "tickets")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Ticket {
	@Id 
	private String id;
	private String displayId;
	@NotBlank(message = "Title is required")
	private String title;
	@NotBlank(message = "Description is required")
	private String description;
	private String categoryId;
	private STATUS status; 
	private PRIORITY priority;						
	private String createdBy; 						
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
