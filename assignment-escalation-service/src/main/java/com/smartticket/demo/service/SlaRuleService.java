package com.smartticket.demo.service;

import org.springframework.http.ResponseEntity;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.SlaRule;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SlaRuleService {

	Mono<ResponseEntity<ApiResponse>> addRule(SlaRule rule);

	Mono<ResponseEntity<ApiResponse>> updateRuleById(String id, SlaRule rule);

	Mono<ResponseEntity<ApiResponse>> deleteRuleById(String id);

	Flux<SlaRule> getAllRules();

}
