package com.smartticket.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.smartticket.demo.entity.SlaRule;
import com.smartticket.demo.enums.PRIORITY;
import com.smartticket.demo.repository.SlaRuleRepository;
import com.smartticket.demo.service.implementation.SlaRuleServiceImplementation;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class SlaRuleServiceImplementationTest {
	@Mock
	private SlaRuleRepository slaRuleRepo;
	@InjectMocks
	private SlaRuleServiceImplementation service;

	@Test
	void addRule_success() {
		SlaRule rule = new SlaRule();
		rule.setPriority(PRIORITY.HIGH);
		rule.setResponseMinutes(10);
		rule.setResolutionMinutes(30);
		when(slaRuleRepo.findByPriority(PRIORITY.HIGH)).thenReturn(Mono.empty());
		when(slaRuleRepo.save(rule)).thenReturn(Mono.just(rule));
		StepVerifier.create(service.addRule(rule)).assertNext(resp -> {
			assertEquals(HttpStatus.OK, resp.getStatusCode());
			assertTrue(resp.getBody().isSuccess());
			assertEquals("SLA rule created successfully", resp.getBody().getMessage());
		}).verifyComplete();
	}

	@Test
	void addRule_alreadyExists() {
		SlaRule rule = new SlaRule();
		rule.setPriority(PRIORITY.HIGH);

		when(slaRuleRepo.findByPriority(PRIORITY.HIGH)).thenReturn(Mono.just(rule));
		when(slaRuleRepo.save(any(SlaRule.class))).thenReturn(Mono.just(rule));

		StepVerifier.create(service.addRule(rule)).assertNext(resp -> {
			assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
			assertFalse(resp.getBody().isSuccess());
			assertTrue(resp.getBody().getMessage().contains("already exists"));
		}).verifyComplete();
	}

	@Test
	void updateRule_success() {
		SlaRule existing = new SlaRule();
		existing.setId("1");
		existing.setPriority(PRIORITY.HIGH);
		existing.setResponseMinutes(10);
		existing.setResolutionMinutes(30);
		SlaRule updated = new SlaRule();
		updated.setResponseMinutes(20);
		updated.setResolutionMinutes(40);
		when(slaRuleRepo.findById("1")).thenReturn(Mono.just(existing));
		when(slaRuleRepo.save(existing)).thenReturn(Mono.just(existing));
		StepVerifier.create(service.updateRuleById("1", updated)).assertNext(resp -> {
			assertEquals(HttpStatus.OK, resp.getStatusCode());
			assertTrue(resp.getBody().isSuccess());
			assertEquals("SLA rule updated successfully", resp.getBody().getMessage());
			assertEquals(20, existing.getResponseMinutes());
			assertEquals(40, existing.getResolutionMinutes());
		}).verifyComplete();
	}

	@Test
	void updateRule_notFound() {
		SlaRule updated = new SlaRule();
		updated.setResponseMinutes(20);
		updated.setResolutionMinutes(40);
		when(slaRuleRepo.findById("1")).thenReturn(Mono.empty());
		StepVerifier.create(service.updateRuleById("1", updated)).assertNext(resp -> {
			assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
			assertFalse(resp.getBody().isSuccess());
			assertEquals("SLA rule not found", resp.getBody().getMessage());
		}).verifyComplete();
	}

	@Test
	void deleteRule_notFound() {
		when(slaRuleRepo.findById("1")).thenReturn(Mono.empty());
		StepVerifier.create(service.deleteRuleById("1")).assertNext(resp -> {
			assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
			assertFalse(resp.getBody().isSuccess());
			assertEquals("SLA rule not found", resp.getBody().getMessage());
		}).verifyComplete();
	}

	@Test
	void deleteRule_success() {
		SlaRule existing = new SlaRule();
		existing.setId("1");
		when(slaRuleRepo.findById("1")).thenReturn(Mono.just(existing));
		when(slaRuleRepo.delete(existing)).thenReturn(Mono.empty());
		StepVerifier.create(service.deleteRuleById("1")).assertNext(resp -> {
			assertEquals(HttpStatus.OK, resp.getStatusCode());
			assertTrue(resp.getBody().isSuccess());
			assertEquals("SLA rule deleted successfully", resp.getBody().getMessage());
		}).verifyComplete();
	}
}
