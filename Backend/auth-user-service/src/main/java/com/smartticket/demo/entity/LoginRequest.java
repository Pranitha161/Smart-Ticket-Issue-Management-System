package com.smartticket.demo.entity;

import lombok.Data;

@Data
public class LoginRequest {
	
	private String email;
	private String password;
}
