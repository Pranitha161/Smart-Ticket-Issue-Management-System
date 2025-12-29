package com.smartticket.demo.service;

import java.util.List;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.AuthResponse;
import com.smartticket.demo.entity.LoginRequest;
import com.smartticket.demo.entity.SimpleApiResponse;
import com.smartticket.demo.entity.User;

import reactor.core.publisher.Mono;

public interface UserAuthService {
	
	Mono<SimpleApiResponse> login(LoginRequest request);
	
	Mono<SimpleApiResponse> register(User user);
	
	Mono<List<AuthResponse>> getUsers();
	
	Mono<AuthResponse> getUserById(String id);
	
	Mono<AuthResponse> getUserByEmail(String email);
	
	Mono<SimpleApiResponse> updateUserById(String id,AuthResponse user);

	Mono<SimpleApiResponse> requestPasswordReset(String email);

	Mono<SimpleApiResponse> resetPassword(String email, String token);

	Mono<SimpleApiResponse> changePassword(String userName, String oldPassword, String newPassword);
	
	Mono<ApiResponse<User>> me();

	Mono<SimpleApiResponse> deleteUserById(String id); 

}
