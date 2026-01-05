package com.smartticket.demo.dto;

import com.smartticket.demo.enums.STATUS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StatusSummaryDto {
	private STATUS status;
	private long count;
}