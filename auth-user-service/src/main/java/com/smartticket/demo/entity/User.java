package com.smartticket.demo.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.smartticket.demo.enums.AGENT_LEVEL;
import com.smartticket.demo.enums.ROLE;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
	@Id
	private String id;
	@NotBlank
	private String username;
	@NotBlank
	private String password;
	@Email
	private String email;
	private Set<ROLE> roles;
	private boolean enabled;
	private LocalDateTime passwordLastChanged;
	private String resetToken;
	private Instant resetTokenExpiry;
	private AGENT_LEVEL agentLevel;
}
