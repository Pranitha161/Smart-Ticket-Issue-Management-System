package com.smartticket.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CategorySummaryDto {
	private String categoryId;
	private long count;
}

