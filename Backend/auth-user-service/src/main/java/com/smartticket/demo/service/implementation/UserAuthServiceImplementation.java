package com.smartticket.demo.service.implementation;

import org.springframework.http.ResponseEntity;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.LoginRequest;
import com.smartticket.demo.entity.User;
import com.smartticket.demo.service.UserAuthService;

import reactor.core.publisher.Mono;

public class UserAuthServiceImplementation implements UserAuthService{

	@Override
	public Mono<ResponseEntity<ApiResponse<User>>> getUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<ResponseEntity<ApiResponse<Void>>> login(LoginRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<ResponseEntity<ApiResponse<Void>>> register(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<ResponseEntity<ApiResponse<User>>> getUserById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<ResponseEntity<ApiResponse<Void>>> updateUserById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<ResponseEntity<ApiResponse<Void>>> deletetUserById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

}
