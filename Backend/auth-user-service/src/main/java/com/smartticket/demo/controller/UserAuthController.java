package com.smartticket.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartticket.demo.dto.AgentDto;
import com.smartticket.demo.dto.AgentStatsDto;
import com.smartticket.demo.dto.UserStatsDto;
import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.AuthResponse;
import com.smartticket.demo.entity.LoginRequest;
import com.smartticket.demo.entity.User;
import com.smartticket.demo.service.implementation.UserAuthServiceImplementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class UserAuthController {

	private final UserAuthServiceImplementation userAuthService;

	public UserAuthController(UserAuthServiceImplementation userAuthService) {
		this.userAuthService = userAuthService;
	}

	@PostMapping("/auth/register")
	public Mono<ResponseEntity<ApiResponse>> register(@RequestBody User user) {
		return userAuthService.register(user).map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
	}

	@PostMapping("/auth/login")
	public Mono<ResponseEntity<ApiResponse>> login(@RequestBody LoginRequest request) {
		return userAuthService.login(request).map(ResponseEntity::ok);
	}

	@GetMapping("/auth/{id}")
	public Mono<ResponseEntity<String>> getEmail(@PathVariable String id) {
		return userAuthService.getUserEmail(id).map(email -> ResponseEntity.ok(email));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/auth/{id}")
	public Mono<ResponseEntity<ApiResponse>> deleteUserById(@PathVariable String id) {
		return userAuthService.deleteUserById(id).map(ResponseEntity::ok);
	}

	@PostMapping("/auth/request-reset")
	public Mono<ResponseEntity<ApiResponse>> requestPasswordReset(@RequestParam String email) {
		return userAuthService.requestPasswordReset(email).map(ResponseEntity::ok);
	}

	@PostMapping("/auth/reset-password")
	public Mono<ResponseEntity<ApiResponse>> resetPassword(@RequestParam String token,
			@RequestParam String newPassword) {
		return userAuthService.resetPassword(token, newPassword).map(ResponseEntity::ok);
	}

	@PostMapping("/auth/change-password")
	public Mono<ResponseEntity<ApiResponse>> changePassword(@RequestParam String userName,
			@RequestParam String oldPassword, @RequestParam String newPassword) {
		return userAuthService.changePassword(userName, oldPassword, newPassword)
				.map(ResponseEntity::ok);
	}

	@GetMapping("/users")
	public Mono<ResponseEntity<List<AuthResponse>>> getUsers() {
		return userAuthService.getUsers().map(ResponseEntity::ok);
	}

	@GetMapping("/users/{id}")
	public Mono<ResponseEntity<AuthResponse>> getUserById(@PathVariable String id) {
		return userAuthService.getUserById(id).map(ResponseEntity::ok);
	}

	@GetMapping("/users/email/{email}")
	public Mono<ResponseEntity<AuthResponse>> getUserByEmail(@PathVariable String email) {
		return userAuthService.getUserByEmail(email).map(ResponseEntity::ok);
	}

	@PutMapping("/users/{id}")
	public Mono<ResponseEntity<ApiResponse>> updateUserById(@PathVariable String id, @RequestBody AuthResponse user) {
		return userAuthService.updateUserById(id, user).map(ResponseEntity::ok);
	}

	@PostMapping("/create")
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<ResponseEntity<ApiResponse>> create(@RequestBody User agent) {
		return userAuthService.register(agent).map(ResponseEntity::ok);
	}

	@PutMapping("/agents/{agentId}/increment-assignments")
	public Mono<ResponseEntity<ApiResponse>> incrementAssignments(@PathVariable String agentId) {
		return userAuthService.incrementAssignments(agentId)
				.map(agent -> ResponseEntity.ok(new ApiResponse(true, "Agent assignment count incremented")));
	}

	@PutMapping("/agents/{agentId}/decrement-assignments")
	public Mono<ResponseEntity<ApiResponse>> decrementAssignments(@PathVariable String agentId) {
		return userAuthService.decrementAssignments(agentId)
				.map(agent -> ResponseEntity.ok(new ApiResponse(true, "Agent assignment count decremented")));
	}

	@PutMapping("/agents/{agentId}/resolved")
	public Mono<ResponseEntity<ApiResponse>> incrementResolvedCount(@PathVariable String agentId) {
		return userAuthService.incrementResolvedCount(agentId)
				.map(agent -> ResponseEntity.ok(new ApiResponse(true, "Agent resolved count incremented")));
	}
	
	@PutMapping("/agents/{agentId}/unresolved")
	public Mono<ResponseEntity<ApiResponse>> incrementEscalatedCount(@PathVariable String agentId) {
		return userAuthService.incrementEscalatedCount(agentId)
				.map(agent -> ResponseEntity.ok(new ApiResponse(true, "Agent escalted count increment")));
	}

	@GetMapping("/dashboard/auth/users/stats")
	public Mono<ResponseEntity<UserStatsDto>> getUserStats() {
		return userAuthService.getUserStats().map(ResponseEntity::ok);
	}

	@GetMapping("/tickets/debug-auth")
	public Mono<ResponseEntity<String>> debugAuth(Authentication auth) {
		if (auth == null) {
			return Mono.just(ResponseEntity.ok("No authentication found"));
		}
		String roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(", "));
		return Mono.just(ResponseEntity.ok("Ticket endpoint authorities: [" + roles + "]"));
	}

	@GetMapping("/agents")
	public Flux<AgentDto> getAgentsByCategory(@RequestParam String category) {
		return userAuthService.getAgentsByCategory(category).map(user -> {
			AgentDto dto = new AgentDto();
			dto.setId(user.getId());
			dto.setUsername(user.getUsername());
			dto.setEmail(user.getEmail());
			dto.setAgentProfile(user.getAgentProfile());
			return dto;
		});
	}

	@PutMapping("/{id}/enable")
	public Mono<ResponseEntity<ApiResponse>> enableUser(@PathVariable String id) {
		return userAuthService.enableUserById(id).map(ResponseEntity::ok).onErrorResume(ex -> Mono
				.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, ex.getMessage()))));
	}

	@GetMapping("/dashboard/{agentId}/stats")
	public Mono<AgentStatsDto> getAgentStats(@PathVariable String agentId) {
		return userAuthService.getAgentStats(agentId);
	}

	@GetMapping("/dashboard/stats")
	public Mono<List<AgentStatsDto>> getAllAgentStats() {
		return userAuthService.getAllAgentStats();
	}

}
