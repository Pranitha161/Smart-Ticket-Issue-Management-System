package com.smartticket.demo.service.implementation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.SlaRule;
import com.smartticket.demo.repository.SlaRuleRepository;
import com.smartticket.demo.service.SlaRuleService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SlaRuleServiceImplementation implements SlaRuleService {

	private final SlaRuleRepository slaRuleRepo;

	SlaRuleServiceImplementation(SlaRuleRepository slaRuleRepo) {
		this.slaRuleRepo = slaRuleRepo;
	}

	@Override
	public Mono<ResponseEntity<ApiResponse>> addRule(SlaRule rule) {
		return slaRuleRepo.findByPriority(rule.getPriority())
				.flatMap(existing -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new ApiResponse(false, "SLA rule already exists for priority: " + rule.getPriority()))))
				.switchIfEmpty(slaRuleRepo.save(rule)
						.map(saved -> ResponseEntity.ok(new ApiResponse(true, "SLA rule created successfully"))));
	}

	@Override
	public Mono<ResponseEntity<ApiResponse>> updateRuleById(String id, SlaRule updatedRule) {
		return slaRuleRepo.findById(id).flatMap(existing -> {
			existing.setResponseMinutes(updatedRule.getResponseMinutes());
			existing.setResolutionMinutes(updatedRule.getResolutionMinutes());
			return slaRuleRepo.save(existing)
					.map(saved -> ResponseEntity.ok(new ApiResponse(true, "SLA rule updated successfully")));
		}).switchIfEmpty(Mono
				.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "SLA rule not found"))));
	}

	@Override
	public Mono<ResponseEntity<ApiResponse>> deleteRuleById(String id) {
		return slaRuleRepo.findById(id)
				.flatMap(existing -> slaRuleRepo.delete(existing)
						.then(Mono.just(ResponseEntity.status(HttpStatus.OK)
								.body(new ApiResponse(true, "SLA rule deleted successfully"))))
						.switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
								.body(new ApiResponse(false, "SLA rule not found")))));
	}

	@Override
	public Flux<SlaRule> getAllRules() {
		return slaRuleRepo.findAll();
	}

}
