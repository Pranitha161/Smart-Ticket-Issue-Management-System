package com.smartticket.demo.service.implementation;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.AuthResponse;
import com.smartticket.demo.entity.LoginRequest;
import com.smartticket.demo.entity.SimpleApiResponse;
import com.smartticket.demo.entity.User;
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
		return new AuthResponse(user.getId(), user.getEmail(), user.getUsername(), user.getRoles());
	}

	@Override
	public Mono<ResponseEntity<SimpleApiResponse>> login(LoginRequest request) {
		return userauthRepo.findByEmail(request.getEmail()).flatMap(existing -> {
			if (!passwordEncoder.matches(request.getPassword(), existing.getPassword())) {
				return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(new SimpleApiResponse(false, "Invalid credentials")));
			}
			if (existing.getPasswordLastChanged() != null) {
				long days = ChronoUnit.DAYS.between(existing.getPasswordLastChanged(), LocalDateTime.now());
				if (days > 90) {
					return Mono.just(ResponseEntity.ok(new SimpleApiResponse(false, "PASSWORD_EXPIRED")));
				}
			}
			String token = jwtService.generateToken(existing);
			return Mono.just(ResponseEntity.ok(new SimpleApiResponse(true, token)));
		}).switchIfEmpty(Mono.just(
				ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SimpleApiResponse(false, "User not found"))));
	}

	@Override
	public Mono<ResponseEntity<SimpleApiResponse>> register(User user) {
		return userauthRepo.findByEmail(user.getEmail()).flatMap(existing -> {
			return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new SimpleApiResponse(false, "Email already exists")));
		}).switchIfEmpty(userauthRepo.findByUsername(user.getUsername()).flatMap(existing -> {
			return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new SimpleApiResponse(false, "Username already exists")));
		}).switchIfEmpty(Mono.defer(() -> {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			user.setPasswordLastChanged(LocalDateTime.now());
			user.setEnabled(true);
			return userauthRepo.save(user).doOnSuccess(
					saved -> eventPublisher.publishUserRegistered(saved.getId(), saved.getEmail(), saved.getUsername()))
					.then(Mono.just(ResponseEntity.status(HttpStatus.CREATED)
							.body(new SimpleApiResponse(true, "User created successfully"))));

		})));
	}

	@Override
	public Mono<ResponseEntity<ApiResponse<List<User>>>> getUsers() {
		return userauthRepo.findAll().collectList().map(
				users -> ResponseEntity.ok(new ApiResponse<>("SUCCESS", 200, "Fetched users successfully", users)));
	}

	@Override
	public Mono<ResponseEntity<AuthResponse>> getUserById(String id) {
		return userauthRepo.findById(id).map(user -> ResponseEntity.ok(toResponse(user)))
				.switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

	@Override
	public Mono<ResponseEntity<AuthResponse>> getUserByEmail(String email) {
		return userauthRepo.findByEmail(email).map(user -> ResponseEntity.ok(toResponse(user)))
				.switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

	@Override
	public Mono<ResponseEntity<SimpleApiResponse>> updateUserById(String id, AuthResponse user) {
		return userauthRepo.findById(id)
				.flatMap(existing -> userauthRepo.findByUsername(user.getUsername())
						.filter(other -> !other.getId().equals(id))
						.flatMap(conflict -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body(new SimpleApiResponse(false, "Username already exists"))))
						.switchIfEmpty(Mono.defer(() -> {
							existing.setRoles(user.getRoles());
							existing.setEmail(user.getEmail());
							existing.setUsername(user.getUsername());

							return userauthRepo.save(existing).map(saved -> ResponseEntity
									.ok(new SimpleApiResponse(true, "User updated successfully")));
						})))
				.switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new SimpleApiResponse(false, "User not found"))));
	}

	@Override
	public Mono<ResponseEntity<SimpleApiResponse>> deletetUserById(String id) {
		return userauthRepo.findById(id).flatMap(existing -> {
			if (!existing.isEnabled()) {
				return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new SimpleApiResponse(false, "User already disabled")));
			}
			existing.setEnabled(false);
			return userauthRepo.save(existing)
					.map(saved -> ResponseEntity.ok(new SimpleApiResponse(true, "User disabled successfully")));
		}).switchIfEmpty(Mono.just(
				ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SimpleApiResponse(false, "User not found"))));
	}

	@Override
	public Mono<ResponseEntity<SimpleApiResponse>> requestPasswordReset(String email) {
		return userauthRepo.findByEmail(email).flatMap(user -> {
			String token = UUID.randomUUID().toString();
			user.setResetToken(token);
			user.setResetTokenExpiry(Instant.now().plus(Duration.ofMinutes(15)));

			return userauthRepo.save(user)
					.doOnSuccess(savedUser -> eventPublisher.publishPasswordReset("userId", email, token))
					.thenReturn(ResponseEntity.ok(new SimpleApiResponse(true, "Reset link sent to your email")));
		}).switchIfEmpty(Mono.just(
				ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SimpleApiResponse(false, "User not found"))));
	}

	@Override
	public Mono<ResponseEntity<SimpleApiResponse>> resetPassword(String token, String newPassword) {
		return userauthRepo.findByResetToken(token).flatMap(user -> {
			if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(Instant.now())) {
				return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new SimpleApiResponse(false, "Invalid or expired token")));
			}
			user.setPassword(passwordEncoder.encode(newPassword));
			user.setPasswordLastChanged(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
			user.setResetToken(null);
			user.setResetTokenExpiry(null);
			return userauthRepo.save(user)
					.thenReturn(ResponseEntity.ok(new SimpleApiResponse(true, "Password reset successful")));
		}).switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new SimpleApiResponse(false, "Invalid or expired token"))));
	}

	@Override
	public Mono<ResponseEntity<SimpleApiResponse>> changePassword(String userName, String oldPassword,
			String newPassword) {
		return userauthRepo.findByUsername(userName).flatMap(user -> {
			if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
				return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new SimpleApiResponse(false, "Invalid current password")));
			}
			user.setPassword(passwordEncoder.encode(newPassword));
			user.setPasswordLastChanged(LocalDateTime.now());
			return userauthRepo.save(user).doOnSuccess(saved -> {
				eventPublisher.publishPasswordChanged(saved.getId(), saved.getEmail());
			}).thenReturn(ResponseEntity.ok(new SimpleApiResponse(true, "Password changed successfully")));
		}).switchIfEmpty(Mono.just(
				ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SimpleApiResponse(false, "User not found"))));
	}

	@Override
	public Mono<ResponseEntity<ApiResponse<User>>> me() {
		// TODO Auto-generated method stub
		return null;
	}

}
