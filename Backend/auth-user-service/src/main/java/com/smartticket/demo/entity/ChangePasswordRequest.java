package com.smartticket.demo.entity;

import lombok.Data;

@Data
public class ChangePasswordRequest {
	
	private String userName;
	private String oldPassword;
	private String newPassword;
}
