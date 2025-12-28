package com.smartticket.demo.entity;

import java.util.Set;

import com.smartticket.demo.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
	private String id;
	private String email;
	private String username;
	private Set<Role> roles;
}
