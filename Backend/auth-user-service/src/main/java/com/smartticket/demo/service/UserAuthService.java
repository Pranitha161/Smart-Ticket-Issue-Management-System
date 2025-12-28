package com.smartticket.demo.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.AuthResponse;
import com.smartticket.demo.entity.LoginRequest;
import com.smartticket.demo.entity.SimpleApiResponse;
import com.smartticket.demo.entity.User;

import reactor.core.publisher.Mono;

public interface UserAuthService {
	
	Mono<ResponseEntity<SimpleApiResponse>> login(LoginRequest request);
	
	Mono<ResponseEntity<SimpleApiResponse>> register(User user);
	
	Mono<ResponseEntity<ApiResponse<List<User>>>> getUsers();
	
	Mono<ResponseEntity<AuthResponse>> getUserById(String id);
	
	Mono<ResponseEntity<AuthResponse>> getUserByEmail(String email);
	
	Mono<ResponseEntity<SimpleApiResponse>> updateUserById(String id,AuthResponse user);
	
	Mono<ResponseEntity<SimpleApiResponse>> deletetUserById(String id);

	Mono<ResponseEntity<SimpleApiResponse>> requestPasswordReset(String email);

	Mono<ResponseEntity<SimpleApiResponse>> resetPassword(String email, String token);

	Mono<ResponseEntity<SimpleApiResponse>> changePassword(String userName, String oldPassword, String newPassword);
	
	Mono<ResponseEntity<ApiResponse<User>>> me(); 

}
