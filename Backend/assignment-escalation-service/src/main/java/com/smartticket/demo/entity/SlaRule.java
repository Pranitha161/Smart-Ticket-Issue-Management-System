package com.smartticket.demo.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import com.smartticket.demo.enums.PRIORITY;

import lombok.Data;

@Document(collection = "sla_rules")
@Data
public class SlaRule {
	private PRIORITY priority;
	private int responseMinutes;
	private int resolutionMinutes;
}
