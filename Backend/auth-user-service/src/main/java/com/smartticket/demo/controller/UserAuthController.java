package com.smartticket.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.AuthResponse;
import com.smartticket.demo.entity.ChangePasswordRequest;
import com.smartticket.demo.entity.LoginRequest;
import com.smartticket.demo.entity.SimpleApiResponse;
import com.smartticket.demo.entity.User;
import com.smartticket.demo.service.implementation.UserAuthServiceImplementation;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
public class UserAuthController {
	private final UserAuthServiceImplementation userauthService;

	public UserAuthController(UserAuthServiceImplementation authService){
		this.userauthService = authService; }
	
	@PostMapping("/auth/register")
	public Mono<ResponseEntity<SimpleApiResponse>> register(@Valid @RequestBody User user){
		return userauthService.register(user);
	}
	
	@PostMapping("/auth/login")
	public Mono<ResponseEntity<SimpleApiResponse>> login(@Valid @RequestBody LoginRequest loginRequest){
		return userauthService.login(loginRequest);
	}
	
	@GetMapping("/users")
	public Mono<ResponseEntity<ApiResponse<List<User>>>> getUsers(){
		return userauthService.getUsers();
	}
	
	@GetMapping("/users/{userId}")
	public Mono<ResponseEntity<AuthResponse>> getUserById(@PathVariable String userId){
		return userauthService.getUserById(userId);
	}
	
//	@GetMapping("/me")
//	public Mono<ResponseEntity<ApiResponse<Void>>> getLoggedInUser(JwtAuth)
	
	@PostMapping("/auth/request-reset")
	public Mono<ResponseEntity<SimpleApiResponse>> requestReset(@RequestBody Map<String, String> body) {
		return userauthService.requestPasswordReset(body.get("email"));
	}

	@PostMapping("/auth/reset-password")
	public Mono<ResponseEntity<SimpleApiResponse>> resetPassword(@RequestBody Map<String, String> body) {
		return userauthService.resetPassword(body.get("token"), body.get("newPassword"));
	}
	
	@PostMapping("/auth/change-password")
	public Mono<ResponseEntity<SimpleApiResponse>> changePassword(@RequestBody ChangePasswordRequest request) {
		return userauthService.changePassword(request.getUserName(), request.getOldPassword(), request.getNewPassword());
	}
}
