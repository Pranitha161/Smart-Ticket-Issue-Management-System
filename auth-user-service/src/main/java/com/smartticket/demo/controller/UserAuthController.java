package com.smartticket.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartticket.demo.entity.AuthResponse;
import com.smartticket.demo.entity.LoginRequest;
import com.smartticket.demo.entity.SimpleApiResponse;
import com.smartticket.demo.entity.User;
import com.smartticket.demo.service.implementation.UserAuthServiceImplementation;

import reactor.core.publisher.Mono;

@RestController
public class UserAuthController {
	private final UserAuthServiceImplementation userAuthService;

	public UserAuthController(UserAuthServiceImplementation userAuthService) {
		this.userAuthService = userAuthService;
	}

	@PostMapping("/auth/register")
	public Mono<ResponseEntity<SimpleApiResponse>> register(@RequestBody User user) {
		return userAuthService.register(user).map(response -> {
			HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.CONFLICT;
			return ResponseEntity.status(status).body(response);
		});
	}

	@PostMapping("/auth/login")
	public Mono<ResponseEntity<SimpleApiResponse>> login(@RequestBody LoginRequest request) {
		return userAuthService.login(request).map(response -> {
			HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
			return ResponseEntity.status(status).body(response);
		});
	}

	@GetMapping("/users")
	public Mono<ResponseEntity<List<AuthResponse>>> getUsers() {
		return userAuthService.getUsers().map(users -> ResponseEntity.ok(users));
	}

	@GetMapping("/users/{id}")
	public Mono<ResponseEntity<AuthResponse>> getUserById(@PathVariable String id) {
		return userAuthService.getUserById(id).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping("/users/email/{email}")
	public Mono<ResponseEntity<AuthResponse>> getUserByEmail(@PathVariable String email) {
		return userAuthService.getUserByEmail(email).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PutMapping("/users/{id}")
	public Mono<ResponseEntity<SimpleApiResponse>> updateUserById(@PathVariable String id,
			@RequestBody AuthResponse user) {
		return userAuthService.updateUserById(id, user).map(response -> {
			HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
			return ResponseEntity.status(status).body(response);
		});
	}

	@DeleteMapping("/auth/{id}")
	public Mono<ResponseEntity<SimpleApiResponse>> deleteUserById(@PathVariable String id) {
		return userAuthService.deleteUserById(id).map(response -> {
			HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
			return ResponseEntity.status(status).body(response);
		});
	}

//	@GetMapping("/me")
//	public Mono<ResponseEntity<ApiResponse<Void>>> getLoggedInUser(JwtAuth)

	@PostMapping("/auth/request-reset")
	public Mono<ResponseEntity<SimpleApiResponse>> requestPasswordReset(@RequestParam String email) {
		return userAuthService.requestPasswordReset(email).map(response -> {
			HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
			return ResponseEntity.status(status).body(response);
		});
	}

	@PostMapping("/auth/reset-password")
	public Mono<ResponseEntity<SimpleApiResponse>> resetPassword(@RequestParam String token,
			@RequestParam String newPassword) {
		return userAuthService.resetPassword(token, newPassword).map(response -> {
			HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
			return ResponseEntity.status(status).body(response);
		});
	}

	@PostMapping("/auth/change-password")
	public Mono<ResponseEntity<SimpleApiResponse>> changePassword(@RequestParam String userName,
			@RequestParam String oldPassword, @RequestParam String newPassword) {
		return userAuthService.changePassword(userName, oldPassword, newPassword).map(response -> {
			HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
			return ResponseEntity.status(status).body(response);
		});
	}

//	@GetMapping("/me")
//	public Mono<ResponseEntity<AuthResponse>> me() {
//		return userAuthService.me().map(ResponseEntity::ok)
//				.defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
//	}
}
