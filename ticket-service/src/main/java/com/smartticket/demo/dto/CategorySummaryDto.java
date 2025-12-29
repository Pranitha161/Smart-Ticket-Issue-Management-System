package com.smartticket.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategorySummaryDto {
	private String categoryId;
	private long count;
}
