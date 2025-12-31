package com.smartticket.demo.entity;

import java.util.Set;

import com.smartticket.demo.enums.ROLE;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
	private String id;
	private String displayId;
	private String email;
	private String username;
	private boolean enabled;
	private Set<ROLE> roles;
}
