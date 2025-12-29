package com.smartticket.demo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.smartticket.demo.entity.User;

import reactor.core.publisher.Mono;

@Repository
public interface UserAuthRepository extends ReactiveMongoRepository<User, String> {
	
	Mono<User> findByEmail(String email);
	
	Mono<User> findByUsername(String username);
	
	Mono<User> findByResetToken(String token);

}
