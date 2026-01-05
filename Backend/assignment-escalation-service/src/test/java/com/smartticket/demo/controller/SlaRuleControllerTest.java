package com.smartticket.demo.controller;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.SlaRule;
import com.smartticket.demo.enums.PRIORITY;
import com.smartticket.demo.service.implementation.SlaRuleServiceImplementation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SlaRuleControllerTest {
	@Mock
	private SlaRuleServiceImplementation slaRuleService;
	@InjectMocks
	private SlaRuleController controller;

	@Test
	void addRule_success() {
		SlaRule rule = new SlaRule();
		rule.setId("1");
		rule.setPriority(PRIORITY.HIGH);
		rule.setResolutionMinutes(30);
		rule.setResponseMinutes(10);
		ApiResponse response = new ApiResponse(true, "SLA rule created successfully");
		when(slaRuleService.addRule(rule)).thenReturn(Mono.just(ResponseEntity.ok(response)));
		StepVerifier.create(controller.addRule(rule)).assertNext(resp -> {
			assertEquals(HttpStatus.OK, resp.getStatusCode());
			assertTrue(resp.getBody().isSuccess());
			assertEquals("SLA rule created successfully", resp.getBody().getMessage());
		}).verifyComplete();
	}

	@Test
	void updateRule_success() {

		SlaRule rule = new SlaRule();
		rule.setId("1");
		rule.setPriority(PRIORITY.HIGH);
		rule.setResolutionMinutes(40);
		rule.setResponseMinutes(20);
		ApiResponse response = new ApiResponse(true, "SLA rule updated successfully");
		when(slaRuleService.updateRuleById("1", rule)).thenReturn(Mono.just(ResponseEntity.ok(response)));
		StepVerifier.create(controller.updateRule("1", rule)).assertNext(resp -> {
			assertEquals(HttpStatus.OK, resp.getStatusCode());
			assertTrue(resp.getBody().isSuccess());
			assertEquals("SLA rule updated successfully", resp.getBody().getMessage());
		}).verifyComplete();
	}

	@Test
	void deleteRule_success() {
		ApiResponse response = new ApiResponse(true, "SLA rule deleted successfully");
		when(slaRuleService.deleteRuleById("1")).thenReturn(Mono.just(ResponseEntity.ok(response)));
		StepVerifier.create(controller.deleteRule("1")).assertNext(resp -> {
			assertEquals(HttpStatus.OK, resp.getStatusCode());
			assertTrue(resp.getBody().isSuccess());
			assertEquals("SLA rule deleted successfully", resp.getBody().getMessage());
		}).verifyComplete();
	}

	@Test
	void getAllRules_success() {
		SlaRule rule1 = new SlaRule();
		rule1.setId("1");
		rule1.setPriority(PRIORITY.HIGH);
		rule1.setResolutionMinutes(30);
		rule1.setResponseMinutes(10);
		SlaRule rule2 = new SlaRule();
		rule2.setId("1");
		rule2.setPriority(PRIORITY.LOW);
		rule2.setResolutionMinutes(15);
		rule2.setResponseMinutes(5);

		when(slaRuleService.getAllRules()).thenReturn(Flux.just(rule1, rule2));
		StepVerifier.create(controller.getAllRules()).expectNext(rule1).expectNext(rule2).verifyComplete();
	}
}
