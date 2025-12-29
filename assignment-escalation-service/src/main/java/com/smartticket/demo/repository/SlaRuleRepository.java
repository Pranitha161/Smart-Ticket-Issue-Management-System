package com.smartticket.demo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.smartticket.demo.entity.SlaRule;
import com.smartticket.demo.enums.PRIORITY;

import reactor.core.publisher.Mono;

@Repository
public interface SlaRuleRepository extends ReactiveMongoRepository<SlaRule, String> {
	Mono<SlaRule> findByPriority(PRIORITY priority);
}
