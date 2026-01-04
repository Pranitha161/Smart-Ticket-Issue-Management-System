package com.smartticket.demo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.smartticket.demo.enums.PRIORITY;

import lombok.Data;

@Document(collection = "sla_rules")
@Data
public class SlaRule {
	@Id
	private String id;
	private PRIORITY priority;
	private int responseMinutes;
	private int resolutionMinutes;
}
