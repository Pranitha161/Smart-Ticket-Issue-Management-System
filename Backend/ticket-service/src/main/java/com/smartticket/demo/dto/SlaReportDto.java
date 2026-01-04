package com.smartticket.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlaReportDto {
	private String ticketId;
	private long resolutionMinutes;
	
}
