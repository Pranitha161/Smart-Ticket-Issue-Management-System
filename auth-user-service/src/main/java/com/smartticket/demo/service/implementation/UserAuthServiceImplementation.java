package com.smartticket.demo.service.implementation;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.Document;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartticket.demo.entity.AuthResponse;
import com.smartticket.demo.entity.LoginRequest;
import com.smartticket.demo.dto.UserStatsDto;
import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.User;
import com.smartticket.demo.enums.ROLE;
import com.smartticket.demo.producer.UserEventPublisher;
import com.smartticket.demo.repository.UserAuthRepository;
import com.smartticket.demo.service.JwtService;
import com.smartticket.demo.service.UserAuthService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserAuthServiceImplementation implements UserAuthService {

	private final UserAuthRepository userauthRepo;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final UserEventPublisher eventPublisher;

	private AuthResponse toResponse(User user) {
		return new AuthResponse(user.getId(), user.getDisplayId(), user.getEmail(), user.getUsername(),
				user.isEnabled(), user.getRoles());
	}

	@Override
	public Mono<ApiResponse> login(LoginRequest request) {
		return userauthRepo.findByEmail(request.getEmail()).flatMap(existing -> {
			if (!passwordEncoder.matches(request.getPassword(), existing.getPassword())) {
				return Mono.just(new ApiResponse(false, "Invalid credentials"));
			}
			if (existing.getPasswordLastChanged() != null) {
				long days = ChronoUnit.DAYS.between(existing.getPasswordLastChanged(), LocalDateTime.now());
				if (days > 90) {
					return Mono.just(new ApiResponse(false, "PASSWORD_EXPIRED"));
				}
			}
			String token = jwtService.generateToken(existing);
			return Mono.just(new ApiResponse(true, token));
		}).switchIfEmpty(Mono.just(new ApiResponse(false, "User not found")));
	}

	@Override
	public Mono<ApiResponse> register(User user) {
		return userauthRepo.findByEmail(user.getEmail())
				.flatMap(existing -> Mono.just(new ApiResponse(false, "Email already exists")))
				.switchIfEmpty(userauthRepo.findByUsername(user.getUsername())
						.flatMap(existing -> Mono.just(new ApiResponse(false, "Username already exists")))
						.switchIfEmpty(Mono.defer(() -> {
							if (user.getRoles().contains(ROLE.AGENT) && user.getAgentLevel() == null) {
								return Mono.just(new ApiResponse(false, "Agent must have an AgentLevel (L1/L2/L3)"));
							}
							user.setPassword(passwordEncoder.encode(user.getPassword()));
							user.setPasswordLastChanged(LocalDateTime.now());
							user.setEnabled(true);
							System.out.println("Hello");
							return userauthRepo.save(user).flatMap(saved -> {
								System.out.println(saved.getId());
								saved.setDisplayId("USR-" + saved.getId().substring(0, 6).toUpperCase());
								return userauthRepo.save(saved)
										.doOnSuccess(u -> eventPublisher.publishUserRegistered(u.getId(), u.getEmail(),
												u.getUsername()))
										.thenReturn(new ApiResponse(true, "User created successfully"));
							});

						})));
	}

	@Override
	public Mono<List<AuthResponse>> getUsers() {
		return userauthRepo.findAll().map(this::toResponse).collectList();
	}

	@Override
	public Mono<AuthResponse> getUserById(String id) {
		return userauthRepo.findById(id).map(this::toResponse);
	}

	@Override
	public Mono<AuthResponse> getUserByEmail(String email) {
		return userauthRepo.findByEmail(email).map(this::toResponse);
	}

	@Override
	public Mono<String> getUserEmail(String id) {
		return userauthRepo.findById(id).map(User::getEmail);
	}

	@Override
	public Mono<ApiResponse> updateUserById(String id, AuthResponse user) {
		return userauthRepo.findById(id)
				.flatMap(existing -> userauthRepo.findByUsername(user.getUsername())
						.filter(other -> !other.getId().equals(id))
						.flatMap(conflict -> Mono.just(new ApiResponse(false, "Username already exists")))
						.switchIfEmpty(Mono.defer(() -> {
							existing.setRoles(user.getRoles());
							existing.setEmail(user.getEmail());
							existing.setUsername(user.getUsername());
							return userauthRepo.save(existing)
									.map(saved -> new ApiResponse(true, "User updated successfully"));
						})))
				.switchIfEmpty(Mono.just(new ApiResponse(false, "User not found")));
	}

	@Override
	public Mono<ApiResponse> requestPasswordReset(String email) {
		return userauthRepo.findByEmail(email).flatMap(user -> {
			String token = UUID.randomUUID().toString();
			user.setResetToken(token);
			user.setResetTokenExpiry(Instant.now().plus(Duration.ofMinutes(15)));
			return userauthRepo.save(user)
					.doOnSuccess(saved -> eventPublisher.publishPasswordReset(saved.getId(), email, token))
					.thenReturn(new ApiResponse(true, "Reset link sent to your email"));
		}).switchIfEmpty(Mono.just(new ApiResponse(false, "User not found")));
	}

	@Override
	public Mono<ApiResponse> resetPassword(String token, String newPassword) {
		return userauthRepo.findByResetToken(token).flatMap(user -> {
			if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(Instant.now())) {
				return Mono.just(new ApiResponse(false, "Invalid or expired token"));
			}
			user.setPassword(passwordEncoder.encode(newPassword));
			user.setPasswordLastChanged(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
			user.setResetToken(null);
			user.setResetTokenExpiry(null);
			return userauthRepo.save(user).thenReturn(new ApiResponse(true, "Password reset successful"));
		}).switchIfEmpty(Mono.just(new ApiResponse(false, "Invalid or expired token")));
	}

	@Override
	public Mono<ApiResponse> changePassword(String userName, String oldPassword, String newPassword) {
		return userauthRepo.findByUsername(userName).flatMap(user -> {
			if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
				return Mono.just(new ApiResponse(false, "Invalid current password"));
			}
			user.setPassword(passwordEncoder.encode(newPassword));
			user.setPasswordLastChanged(LocalDateTime.now());
			return userauthRepo.save(user).doOnSuccess(saved -> {
				eventPublisher.publishPasswordChanged(saved.getId(), saved.getEmail());
			}).thenReturn(new ApiResponse(true, "Password changed successfully"));
		}).switchIfEmpty(Mono.just(new ApiResponse(false, "User not found")));
	}

	@Override
	public Mono<AuthResponse> me() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<ApiResponse> deleteUserById(String id) {
		return userauthRepo.findById(id).flatMap(existing -> {
			if (!existing.isEnabled()) {
				return Mono.just(new ApiResponse(false, "User already disabled"));
			}
			existing.setEnabled(false);
			return userauthRepo.save(existing).map(saved -> new ApiResponse(true, "User disabled successfully"));
		}).switchIfEmpty(Mono.just(new ApiResponse(false, "User not found")));
	}
	@Override
	public Mono<UserStatsDto> getUserStats() {
	    Mono<Long> totalUsersMono = userauthRepo.countTotalUsers()
	        .next()
	        .map(doc -> {
	            Number n = doc.get("totalUsers", Number.class);
	            return n == null ? 0L : n.longValue();
	        })
	        .defaultIfEmpty(0L);

	    Mono<Long> activeUsersMono = userauthRepo.countActiveUsers()
	        .next()
	        .map(doc -> {
	            Number n = doc.get("activeUsers", Number.class);
	            return n == null ? 0L : n.longValue();
	        })
	        .defaultIfEmpty(0L);

	    Mono<Map<String, Long>> roleCountsMono = userauthRepo.countUsersByRole()
	        .collectMap(
	            doc -> doc.getString("_id"),
	            doc -> {
	                Number n = doc.get("count", Number.class);
	                return n == null ? 0L : n.longValue();
	            }
	        );

	    return Mono.zip(totalUsersMono, activeUsersMono, roleCountsMono)
	        .map(tuple -> {
	            long totalUsers = tuple.getT1();
	            long activeUsers = tuple.getT2();
	            Map<String, Long> roleCounts = tuple.getT3();

	            return new UserStatsDto(
	                totalUsers,
	                activeUsers,
	                roleCounts.getOrDefault("AGENT", 0L),
	                roleCounts.getOrDefault("USER", 0L),
	                roleCounts.getOrDefault("MANAGER", 0L),
	                roleCounts.getOrDefault("ADMIN", 0L)
	            );
	        });
	}

}
