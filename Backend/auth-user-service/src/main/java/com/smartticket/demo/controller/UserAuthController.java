package com.smartticket.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.LoginRequest;
import com.smartticket.demo.entity.User;
import com.smartticket.demo.service.implementation.UserAuthServiceImplementation;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import reactor.core.publisher.Mono;

@RestController
public class UserAuthController {
	private final UserAuthServiceImplementation authService;

	public UserAuthController(UserAuthServiceImplementation authService){
		this.authService = authService; }
	
	@PostMapping("/auth/register")
	public Mono<ResponseEntity<ApiResponse<Void>>> register(@RequestBody User user){
		return authService.register(user);
	}
	
	@PostMapping("/auth/login")
	public Mono<ResponseEntity<ApiResponse<Void>>> login(@RequestBody LoginRequest loginRequest){
		return authService.login(loginRequest);
	}
	
	@GetMapping("/users")
	public Mono<ResponseEntity<ApiResponse<User>>> getUsers(){
		return authService.getUsers();
	}
	
	@GetMapping("/users/{userId}")
	public Mono<ResponseEntity<ApiResponse<User>>> getUserById(@PathVariable String userId){
		return authService.getUserById(userId);
	}
	
	
	

}
