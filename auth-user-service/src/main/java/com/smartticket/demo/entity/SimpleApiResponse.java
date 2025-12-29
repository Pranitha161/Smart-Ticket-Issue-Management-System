package com.smartticket.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimpleApiResponse {

	private boolean success;
	private String message;

}
