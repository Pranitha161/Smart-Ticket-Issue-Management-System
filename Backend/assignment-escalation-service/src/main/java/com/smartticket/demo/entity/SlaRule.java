package com.smartticket.demo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.smartticket.demo.enums.PRIORITY;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "sla_rules")
@Data
@NoArgsConstructor
public class SlaRule {
	@Id
	private String id;
	private PRIORITY priority;
	private int responseMinutes;
	private int resolutionMinutes;
}
