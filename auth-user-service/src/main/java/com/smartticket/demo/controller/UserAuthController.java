package com.smartticket.demo.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import reactor.core.publisher.Mono;



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
import com.smartticket.demo.dto.UserStatsDto;
import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.User;
import com.smartticket.demo.service.implementation.UserAuthServiceImplementation;


@RestController
public class UserAuthController {
	private final UserAuthServiceImplementation userAuthService;

	public UserAuthController(UserAuthServiceImplementation userAuthService) {
		this.userAuthService = userAuthService;
	}

	@PostMapping("/auth/register")
	public Mono<ResponseEntity<ApiResponse>> register(@RequestBody User user) {
		return userAuthService.register(user).map(response -> {
			HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.CONFLICT;
			return ResponseEntity.status(status).body(response);
		});
	}

	@PostMapping("/auth/login")
	public Mono<ResponseEntity<ApiResponse>> login(@RequestBody LoginRequest request) {
		return userAuthService.login(request).map(response -> {
			HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
			return ResponseEntity.status(status).body(response);
		});
	}

	@GetMapping("/auth/{id}")
	public Mono<ResponseEntity<String>> getEmail(@PathVariable String id) {
		return userAuthService.getUserEmail(id).map(email -> ResponseEntity.ok(email))
				.switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
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
	public Mono<ResponseEntity<ApiResponse>> updateUserById(@PathVariable String id, @RequestBody AuthResponse user) {
		return userAuthService.updateUserById(id, user).map(response -> {
			HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
			return ResponseEntity.status(status).body(response);
		});
	}

	@DeleteMapping("/auth/{id}")
	public Mono<ResponseEntity<ApiResponse>> deleteUserById(@PathVariable String id) {
		return userAuthService.deleteUserById(id).map(response -> {
			HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
			return ResponseEntity.status(status).body(response);
		});
	}

//	@GetMapping("/me")
//	public Mono<ResponseEntity<ApiResponse<Void>>> getLoggedInUser(JwtAuth)

	@PostMapping("/auth/request-reset")
	public Mono<ResponseEntity<ApiResponse>> requestPasswordReset(@RequestParam String email) {
		return userAuthService.requestPasswordReset(email).map(response -> {
			HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
			return ResponseEntity.status(status).body(response);
		});
	}

	@PostMapping("/auth/reset-password")
	public Mono<ResponseEntity<ApiResponse>> resetPassword(@RequestParam String token,
			@RequestParam String newPassword) {
		return userAuthService.resetPassword(token, newPassword).map(response -> {
			HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
			return ResponseEntity.status(status).body(response);
		});
	}

	@PostMapping("/auth/change-password")
	public Mono<ResponseEntity<ApiResponse>> changePassword(@RequestParam String userName,
			@RequestParam String oldPassword, @RequestParam String newPassword) {
		return userAuthService.changePassword(userName, oldPassword, newPassword).map(response -> {
			HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
			return ResponseEntity.status(status).body(response);
		});
	}
	@GetMapping("/auth/users/stats")
	public Mono<UserStatsDto> getUserStats() {
	    return userAuthService.getUserStats();
	}
	@GetMapping("/tickets/debug-auth")
	public Mono<String> debugAuth(Authentication auth) {
	    if (auth == null) {
	        System.out.println("No authentication found");
	        return Mono.just("No authentication found");
	    }
	    String roles = auth.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .collect(Collectors.joining(", "));
	    System.out.println("Ticket endpoint authorities: [" + roles + "]");
	    return Mono.just(roles);
	}


//	@GetMapping("/me")
//	public Mono<ResponseEntity<AuthResponse>> me() {
//		return userAuthService.me().map(ResponseEntity::ok)
//				.defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
//	}
}
