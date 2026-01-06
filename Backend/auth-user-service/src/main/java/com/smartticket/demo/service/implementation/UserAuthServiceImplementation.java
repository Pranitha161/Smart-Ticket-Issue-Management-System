package com.smartticket.demo.service.implementation;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartticket.demo.dto.AgentStatsDto;
import com.smartticket.demo.dto.UserStatsDto;
import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.AuthResponse;
import com.smartticket.demo.entity.LoginRequest;
import com.smartticket.demo.entity.User;
import com.smartticket.demo.enums.ROLE;
import com.smartticket.demo.feign.CategoryClient;
import com.smartticket.demo.producer.UserEventPublisher;
import com.smartticket.demo.repository.UserAuthRepository;
import com.smartticket.demo.service.JwtService;
import com.smartticket.demo.service.UserAuthService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UserAuthServiceImplementation implements UserAuthService {

	private final UserAuthRepository userauthRepo;
	private final PasswordEncoder passwordEncoder;
	private final CategoryClient categoryClient;
	private final JwtService jwtService;
	private final UserEventPublisher eventPublisher;

	public UserAuthServiceImplementation(UserAuthRepository userauthRepo, CategoryClient categoryClient,
			UserEventPublisher eventPublisher, JwtService jwtService, PasswordEncoder passwordEncoder) {
		this.userauthRepo = userauthRepo;
		this.categoryClient = categoryClient;
		this.eventPublisher = eventPublisher;
		this.jwtService = jwtService;
		this.passwordEncoder = passwordEncoder;
	}

	private AuthResponse toResponse(User user) {
		return new AuthResponse(user.getId(), user.getDisplayId(), user.getEmail(), user.getUsername(),
				user.isEnabled(), user.getRoles(), user.getAgentProfile());
	}

	@Override
	public Mono<ApiResponse> login(LoginRequest request) {
		return userauthRepo.findByEmail(request.getEmail())
				.switchIfEmpty(Mono.error(new RuntimeException("User not found"))).flatMap(existing -> {
					if (!passwordEncoder.matches(request.getPassword(), existing.getPassword())) {
						return Mono.<ApiResponse>error(new RuntimeException("Invalid credentials"));
					}
					if (existing.getPasswordLastChanged() != null) {
						long days = ChronoUnit.DAYS.between(existing.getPasswordLastChanged(), LocalDateTime.now());
						if (days > 90) {
							return Mono.<ApiResponse>error(new RuntimeException("PASSWORD_EXPIRED"));
						}
					}
					String token = jwtService.generateToken(existing);
					return Mono.just(new ApiResponse(true, token));
				});
	}

	@Override
	public Mono<ApiResponse> register(User user) {
		return userauthRepo.findByEmail(user.getEmail())
				.flatMap(existing -> Mono.<ApiResponse>error(new RuntimeException("Email already exists")))
				.switchIfEmpty(userauthRepo.findByUsername(user.getUsername())
						.flatMap(existing -> Mono.error(new RuntimeException("Username already exists"))))
				.switchIfEmpty(Mono.defer(() -> {
					if (user.getRoles().contains(ROLE.AGENT)) {
						if (user.getAgentProfile() == null || user.getAgentProfile().getAgentLevel() == null) {
							return Mono.error(new RuntimeException("Agent must have AgentLevel (L1/L2/L3)"));
						}
						if (user.getAgentProfile().getCategoryId() == null) {
							return Mono.error(new RuntimeException("Agent must have a specialization category"));
						}
						return Mono
								.fromCallable(
										() -> categoryClient.getCategoryById(user.getAgentProfile().getCategoryId()))
								.subscribeOn(Schedulers.boundedElastic()).flatMap(cat -> {
									if (cat == null || cat.getId() == null) {
										return Mono.error(new RuntimeException("Invalid category ID"));
									}
									return saveNewUser(user);
								});
					}
					return saveNewUser(user);
				}));
	}

	private Mono<ApiResponse> saveNewUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setPasswordLastChanged(LocalDateTime.now());
		user.setEnabled(true);
		return userauthRepo.save(user).flatMap(saved -> {
			saved.setDisplayId("USR-" + saved.getId().substring(0, 6).toUpperCase());
			return userauthRepo.save(saved)
					.doOnSuccess(u -> eventPublisher.publishUserRegistered(u.getId(), u.getEmail(), u.getUsername()))
					.thenReturn(new ApiResponse(true, "User created successfully"));
		});
	}

	@Override
	public Mono<User> incrementAssignments(String agentId) {
		return userauthRepo.findById(agentId).switchIfEmpty(Mono.error(new RuntimeException("Agent not found")))
				.flatMap(agent -> {
					agent.getAgentProfile().setCurrentAssignments(agent.getAgentProfile().getCurrentAssignments() + 1);
					return userauthRepo.save(agent);
				});
	}

	@Override
	public Mono<User> decrementAssignments(String agentId) {
		return userauthRepo.findById(agentId).switchIfEmpty(Mono.error(new RuntimeException("Agent not found")))
				.flatMap(agent -> {
					agent.getAgentProfile().setCurrentAssignments(agent.getAgentProfile().getCurrentAssignments() - 1);
					return userauthRepo.save(agent);
				});
	}

	@Override
	public Mono<User> incrementResolvedCount(String agentId) {
		return userauthRepo.findById(agentId).switchIfEmpty(Mono.error(new RuntimeException("Agent not found")))
				.flatMap(agent -> {
					agent.getAgentProfile().setResolvedCount(agent.getAgentProfile().getResolvedCount() + 1);
					return userauthRepo.save(agent);
				});
	}

	@Override
	public Mono<List<AuthResponse>> getUsers() {
		return userauthRepo.findAll().map(this::toResponse).collectList();
	}

	@Override
	public Mono<AuthResponse> getUserById(String id) {
		return userauthRepo.findById(id).switchIfEmpty(Mono.error(new RuntimeException("User not found")))
				.map(this::toResponse);
	}

	@Override
	public Mono<AuthResponse> getUserByEmail(String email) {
		return userauthRepo.findByEmail(email).switchIfEmpty(Mono.error(new RuntimeException("User not found")))
				.map(this::toResponse);
	}

	@Override
	public Mono<String> getUserEmail(String id) {
		return userauthRepo.findById(id).switchIfEmpty(Mono.error(new RuntimeException("User not found")))
				.map(User::getEmail);
	}

	@Override
	public Mono<ApiResponse> updateUserById(String id, AuthResponse user) {
		return userauthRepo.findById(id).switchIfEmpty(Mono.error(new RuntimeException("User not found")))
				.flatMap(existing -> {
					if (!existing.isEnabled()) {
						return Mono.error(new RuntimeException("User is not active"));
					}
					return userauthRepo.findByUsername(user.getUsername()).filter(other -> !other.getId().equals(id))
							.flatMap(conflict -> Mono
									.<ApiResponse>error(new RuntimeException("Username already exists")))
							.switchIfEmpty(Mono.defer(() -> {
								existing.setRoles(user.getRoles());
								existing.setEmail(user.getEmail());
								existing.setUsername(user.getUsername());
								return userauthRepo.save(existing)
										.map(saved -> new ApiResponse(true, "User updated successfully"));
							}));
				});
	}

	@Override
	public Mono<ApiResponse> requestPasswordReset(String email) {
		return userauthRepo.findByEmail(email).switchIfEmpty(Mono.error(new RuntimeException("User not found")))
				.flatMap(user -> {
					String token = UUID.randomUUID().toString();
					user.setResetToken(token);
					user.setResetTokenExpiry(Instant.now().plus(Duration.ofMinutes(15)));
					return userauthRepo.save(user)
							.doOnSuccess(saved -> eventPublisher.publishPasswordReset(saved.getId(), email,
									"http://localhost:4200/auth/reset-password?token=" + token))
							.thenReturn(new ApiResponse(true, "Reset link sent to your email"));
				});
	}

	@Override
	public Mono<ApiResponse> resetPassword(String token, String newPassword) {
		return userauthRepo.findByResetToken(token)
				.switchIfEmpty(Mono.error(new RuntimeException("Invalid or expired token"))).flatMap(user -> {
					if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(Instant.now())) {
						return Mono.<ApiResponse>error(new RuntimeException("Invalid or expired token"));
					}
					user.setPassword(passwordEncoder.encode(newPassword));
					user.setPasswordLastChanged(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
					user.setResetToken(null);
					user.setResetTokenExpiry(null);
					return userauthRepo.save(user).thenReturn(new ApiResponse(true, "Password reset successful"));
				});
	}

	@Override
	public Mono<ApiResponse> changePassword(String userName, String oldPassword, String newPassword) {
		return userauthRepo.findByUsername(userName).switchIfEmpty(Mono.error(new RuntimeException("User not found")))
				.flatMap(user -> {
					if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
						return Mono.<ApiResponse>error(new RuntimeException("Invalid current password"));
					}
					user.setPassword(passwordEncoder.encode(newPassword));
					user.setPasswordLastChanged(LocalDateTime.now());
					return userauthRepo.save(user)
							.doOnSuccess(
									saved -> eventPublisher.publishPasswordChanged(saved.getId(), saved.getEmail()))
							.thenReturn(new ApiResponse(true, "Password changed successfully"));
				});
	}

	@Override
	public Mono<ApiResponse> deleteUserById(String id) {
		return userauthRepo.findById(id).switchIfEmpty(Mono.error(new RuntimeException("User not found")))
				.flatMap(existing -> {
					if (!existing.isEnabled()) {
						return Mono.<ApiResponse>error(new RuntimeException("User already disabled"));
					}
					existing.setEnabled(false);
					return userauthRepo.save(existing)
							.map(saved -> new ApiResponse(true, "User disabled successfully"));
				});
	}

	@Override
	public Mono<UserStatsDto> getUserStats() {
		Mono<Long> totalUsersMono = userauthRepo.countTotalUsers().next().map(doc -> {
			Number n = doc.get("totalUsers", Number.class);
			return n == null ? 0L : n.longValue();
		}).defaultIfEmpty(0L);

		Mono<Long> activeUsersMono = userauthRepo.countActiveUsers().next().map(doc -> {
			Number n = doc.get("activeUsers", Number.class);
			return n == null ? 0L : n.longValue();
		}).defaultIfEmpty(0L);

		Mono<Map<String, Long>> roleCountsMono = userauthRepo.countUsersByRole().collectMap(doc -> doc.getString("_id"),
				doc -> {
					Number n = doc.get("count", Number.class);
					return n == null ? 0L : n.longValue();
				});

		return Mono.zip(totalUsersMono, activeUsersMono, roleCountsMono).map(tuple -> {
			long totalUsers = tuple.getT1();
			long activeUsers = tuple.getT2();
			Map<String, Long> roleCounts = tuple.getT3();

			return new UserStatsDto(totalUsers, activeUsers, roleCounts.getOrDefault("AGENT", 0L),
					roleCounts.getOrDefault("USER", 0L), roleCounts.getOrDefault("MANAGER", 0L),
					roleCounts.getOrDefault("ADMIN", 0L));
		});
	}

	@Override
	public Flux<User> getAgentsByCategory(String category) {
		return userauthRepo.findByRolesContainingAndAgentProfileCategoryId("AGENT", category);
	}

	@Override
	public Mono<AgentStatsDto> getAgentStats(String agentId) {
		return userauthRepo.findById(agentId).flatMap(profile -> {

			if (profile.getRoles() == null || !profile.getRoles().contains(ROLE.AGENT)) {
				return Mono.error(new IllegalAccessException("User is not an agent"));
			}

			int currentAssignments = profile.getAgentProfile().getCurrentAssignments();
			int resolvedCount = profile.getAgentProfile().getResolvedCount();
			double resolutionRate = (resolvedCount + currentAssignments) == 0 ? 0.0
					: (double) resolvedCount / (resolvedCount + currentAssignments);

			return Mono.just(new AgentStatsDto(agentId, profile.getAgentProfile().getAgentLevel(), currentAssignments,
					resolvedCount, resolutionRate));
		});
	}

	@Override
	public Mono<List<AgentStatsDto>> getAllAgentStats() {
		return userauthRepo.findAll()
				.filter(profile -> profile.getRoles() != null && profile.getRoles().contains(ROLE.AGENT))
				.map(profile -> {
					int currentAssignments = profile.getAgentProfile().getCurrentAssignments();
					int resolvedCount = profile.getAgentProfile().getResolvedCount();
					double resolutionRate = (resolvedCount + currentAssignments) == 0 ? 0.0
							: (double) resolvedCount / (resolvedCount + currentAssignments);

					return new AgentStatsDto(profile.getId(), profile.getAgentProfile().getAgentLevel(),
							currentAssignments, resolvedCount, resolutionRate);
				}).collectList();
	}
	
	@Override
	public Mono<ApiResponse> enableUserById(String id) {
		return userauthRepo.findById(id).switchIfEmpty(Mono.error(new RuntimeException("User not found")))
				.flatMap(user -> {
					if (user.isEnabled()) {
						return Mono.error(new RuntimeException("User is already active"));
					}
					user.setEnabled(true);
					return userauthRepo.save(user).map(saved -> new ApiResponse(true, "User enabled successfully"));
				});
	}

	@Override
	public Mono<User> incrementEscalatedCount(String agentId) {
		return userauthRepo.findById(agentId).switchIfEmpty(Mono.error(new RuntimeException("Agent not found")))
				.flatMap(agent -> {
					agent.getAgentProfile().setEscalatedCount(agent.getAgentProfile().getEscalatedCount() + 1);
					return userauthRepo.save(agent);
				});
	}

}
