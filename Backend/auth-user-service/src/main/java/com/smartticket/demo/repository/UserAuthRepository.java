package com.smartticket.demo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.smartticket.demo.entity.User;

import reactor.core.publisher.Mono;

public interface UserAuthRepository extends ReactiveMongoRepository<User, String> {
	
	Mono<User> findByEmail(String email);

}
