package com.smartticket.demo.service;

import java.util.List;

import com.smartticket.demo.entity.AuthResponse;
import com.smartticket.demo.entity.LoginRequest;
import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.User;

import reactor.core.publisher.Mono;

public interface UserAuthService {
	
	Mono<ApiResponse> login(LoginRequest request);
	
	Mono<ApiResponse> register(User user);
	
	Mono<List<AuthResponse>> getUsers();
	
	Mono<AuthResponse> getUserById(String id);
	
	Mono<AuthResponse> getUserByEmail(String email);
	
	Mono<ApiResponse> updateUserById(String id,AuthResponse user);

	Mono<ApiResponse> requestPasswordReset(String email);

	Mono<ApiResponse> resetPassword(String email, String token);

	Mono<ApiResponse> changePassword(String userName, String oldPassword, String newPassword);
	
	Mono<AuthResponse> me();

	Mono<ApiResponse> deleteUserById(String id);

	Mono<String> getUserEmail(String id); 

}
