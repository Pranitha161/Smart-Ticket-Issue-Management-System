package com.smartticket.demo.dto;

import com.smartticket.demo.enums.STATUS;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusSummaryDto {
	private STATUS status;
	private long count;
}
