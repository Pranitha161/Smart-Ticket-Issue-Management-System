package com.smartticket.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.SlaRule;
import com.smartticket.demo.service.implementation.SlaRuleServiceImplementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/sla-rules")
public class SlaRuleController {
	private final SlaRuleServiceImplementation slaRuleService;

	SlaRuleController(SlaRuleServiceImplementation slaRuleService) {
		this.slaRuleService = slaRuleService;
	}

	@PostMapping 
	public Mono<ResponseEntity<ApiResponse>> addRule(@RequestBody SlaRule rule) {
		return slaRuleService.addRule(rule);
	}

	@PutMapping("/{id}") 
	public Mono<ResponseEntity<ApiResponse>> updateRule(@PathVariable String id, @RequestBody SlaRule rule){
		return slaRuleService.updateRuleById(id,rule);
	}
	
	@DeleteMapping("/{id}") 
	public Mono<ResponseEntity<ApiResponse>> deleteRule(@PathVariable String id){
		return slaRuleService.deleteRuleById(id);
	}
	
	@GetMapping 
	public Flux<SlaRule> getAllRules(){
		return slaRuleService.getAllRules();
	}

}
