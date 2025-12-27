package com.smartticket.demo.service;

import org.springframework.http.ResponseEntity;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.LoginRequest;
import com.smartticket.demo.entity.User;

import reactor.core.publisher.Mono;

public interface UserAuthService {
	
	Mono<ResponseEntity<ApiResponse<User>>> getUsers();
	
	Mono<ResponseEntity<ApiResponse<Void>>> login(LoginRequest request);
	
	Mono<ResponseEntity<ApiResponse<Void>>> register(User user);
	
	Mono<ResponseEntity<ApiResponse<User>>> getUserById(String id);
	
	Mono<ResponseEntity<ApiResponse<Void>>> updateUserById(String id);
	
	Mono<ResponseEntity<ApiResponse<Void>>> deletetUserById(String id);
	
//	Mono<ResponseEntity<ApiResponse<Void>>> assignRole(String )
	
	

}
