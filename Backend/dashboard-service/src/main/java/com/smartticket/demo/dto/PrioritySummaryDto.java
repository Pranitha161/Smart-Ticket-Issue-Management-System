package com.smartticket.demo.dto;

import com.smartticket.demo.enums.PRIORITY;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PrioritySummaryDto {
	private PRIORITY priority;
	private long count;
}
