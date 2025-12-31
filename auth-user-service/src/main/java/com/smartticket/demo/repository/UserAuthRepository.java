package com.smartticket.demo.repository;

import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.smartticket.demo.entity.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserAuthRepository extends ReactiveMongoRepository<User, String> {
	
	Mono<User> findByEmail(String email);
	
	Mono<User> findByUsername(String username);
	
	Mono<User> findByResetToken(String token);
	
	@Aggregation(pipeline = { "{ $count: 'totalUsers' }" })
	Flux<Document> countTotalUsers();
	
	@Aggregation(pipeline = {
			  "{ $match: { enabled: true } }",
			  "{ $count: 'activeUsers' }"
			})
			Flux<Document> countActiveUsers();
	@Aggregation(pipeline = {
			  "{ $unwind: '$roles' }",
			  "{ $group: { _id: '$roles', count: { $sum: 1 } } }"
			})
			Flux<Document> countUsersByRole();



}
