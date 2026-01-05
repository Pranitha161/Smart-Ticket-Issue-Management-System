package com.smartticket.demo.dto;

import com.smartticket.demo.enums.PRIORITY;
import com.smartticket.demo.enums.STATUS;

import lombok.Data;

@Data 
public class TicketDto { 
	
	private String id; 
	private PRIORITY priority; 
	private STATUS status; 
	private String categoryId; 
	private Long version;
	
}
