package com.smartticket.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smartticket.demo.entity.AgentProfile;
import com.smartticket.demo.entity.AuthResponse;
import com.smartticket.demo.entity.LoginRequest;
import com.smartticket.demo.entity.User;
import com.smartticket.demo.enums.AGENT_LEVEL;
import com.smartticket.demo.enums.ROLE;
import com.smartticket.demo.feign.CategoryClient;
import com.smartticket.demo.producer.UserEventPublisher;
import com.smartticket.demo.repository.UserAuthRepository;
import com.smartticket.demo.service.implementation.UserAuthServiceImplementation;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class UserAuthServiceImplementationTest {
	@Mock
	private UserAuthRepository userRepo;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private CategoryClient categoryClient;
	@Mock
	private JwtService jwtService;
	@Mock
	private UserEventPublisher eventPublisher;
	@InjectMocks
	private UserAuthServiceImplementation service;

	@Test
	void login_success() {
		User user = User.builder().id("U1").email("test@example.com").username("tester").password("encodedPass")
				.roles(Set.of(ROLE.USER)).passwordLastChanged(LocalDateTime.now()).enabled(true).build();
		LoginRequest req = new LoginRequest("test@example.com", "rawPass");
		when(userRepo.findByEmail("test@example.com")).thenReturn(Mono.just(user));
		when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);
		when(jwtService.generateToken(user)).thenReturn("jwt-token");
		StepVerifier.create(service.login(req)).assertNext(resp -> {
			assertTrue(resp.isSuccess());
			assertEquals("jwt-token", resp.getMessage());
		}).verifyComplete();
	}

	@Test
	void login_userNotFound() {
		LoginRequest req = new LoginRequest("missing@example.com", "pass");
		when(userRepo.findByEmail("missing@example.com")).thenReturn(Mono.empty());
		StepVerifier.create(service.login(req)).expectErrorMatches(ex -> ex.getMessage().equals("User not found"))
				.verify();
	}

	@Test
	void login_passwordExpired() {
		User user = User.builder().email("test@example.com").password("encodedPass")
				.passwordLastChanged(LocalDateTime.now().minusDays(100)).build();
		LoginRequest req = new LoginRequest("test@example.com", "rawPass");
		when(userRepo.findByEmail("test@example.com")).thenReturn(Mono.just(user));
		when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);
		StepVerifier.create(service.login(req)).expectErrorMatches(ex -> ex.getMessage().equals("PASSWORD_EXPIRED"))
				.verify();
	}

	@Test
	void login_invalidPassword() {
		User user = User.builder().email("test@example.com").password("encodedPass").build();
		LoginRequest req = new LoginRequest("test@example.com", "wrong");
		when(userRepo.findByEmail("test@example.com")).thenReturn(Mono.just(user));
		when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);
		StepVerifier.create(service.login(req)).expectErrorMatches(ex -> ex.getMessage().equals("Invalid credentials"))
				.verify();
	}

	@Test
	void register_emailAlreadyExists() {
		User user = User.builder().email("dup@example.com").username("dup").roles(Set.of(ROLE.USER)).build();
		when(userRepo.findByEmail("dup@example.com")).thenReturn(Mono.just(user));
		when(userRepo.findByUsername("dup")).thenReturn(Mono.empty());

		StepVerifier.create(service.register(user))
				.expectErrorMatches(ex -> ex.getMessage().equals("Email already exists")).verify();
	}

	@Test
	void register_usernameAlreadyExists() {
		User user = User.builder().email("new@example.com").username("dup").roles(Set.of(ROLE.USER)).build();
		when(userRepo.findByEmail("new@example.com")).thenReturn(Mono.empty());
		when(userRepo.findByUsername("dup")).thenReturn(Mono.just(user));
		StepVerifier.create(service.register(user))
				.expectErrorMatches(ex -> ex.getMessage().equals("Username already exists")).verify();
	}

	@Test
	void register_agentMissingLevel() {
		AgentProfile profile = AgentProfile.builder().categoryId("C1").build();
		User user = User.builder().email("a@x.com").username("agent").roles(Set.of(ROLE.AGENT)).agentProfile(profile)
				.build();
		when(userRepo.findByEmail("a@x.com")).thenReturn(Mono.empty());
		when(userRepo.findByUsername("agent")).thenReturn(Mono.empty());
		StepVerifier.create(service.register(user))
				.expectErrorMatches(ex -> ex.getMessage().contains("Agent must have AgentLevel")).verify();
	}

	@Test
	void incrementAssignments_success() {
		AgentProfile agent1 = AgentProfile.builder().currentAssignments(2).resolvedCount(5).agentLevel(AGENT_LEVEL.L1)
				.categoryId("C1").build();

		User agent = User.builder().id("A1").roles(Set.of(ROLE.AGENT)).agentProfile(agent1).build();

		when(userRepo.findById("A1")).thenReturn(Mono.just(agent));
		when(userRepo.save(any(User.class))).thenReturn(Mono.just(agent));
		StepVerifier.create(service.incrementAssignments("A1"))
				.assertNext(saved -> assertEquals(3, saved.getAgentProfile().getCurrentAssignments())).verifyComplete();
	}

	@Test
	void getUserById_success() {
		User user = User.builder().id("U1").email("e@x.com").username("u1").roles(Set.of(ROLE.USER)).enabled(true)
				.build();
		when(userRepo.findById("U1")).thenReturn(Mono.just(user));
		StepVerifier.create(service.getUserById("U1")).assertNext(resp -> {
			assertEquals("U1", resp.getId());
			assertEquals("u1", resp.getUsername());
		}).verifyComplete();
	}

	@Test
	void resetPassword_success() {
		User user = User.builder().id("U1").resetToken("token123").resetTokenExpiry(Instant.now().plusSeconds(600))
				.password("old").build();
		when(userRepo.findByResetToken("token123")).thenReturn(Mono.just(user));
		when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");
		when(userRepo.save(any(User.class))).thenReturn(Mono.just(user));
		StepVerifier.create(service.resetPassword("token123", "newPass")).assertNext(resp -> {
			assertTrue(resp.isSuccess());
			assertEquals("Password reset successful", resp.getMessage());
		}).verifyComplete();
	}

	@Test
	void getAgentStats_success() {
		AgentProfile agent1 = AgentProfile.builder().currentAssignments(4).resolvedCount(6).agentLevel(AGENT_LEVEL.L2)
				.build();
		User agent = User.builder().id("A1").roles(Set.of(ROLE.AGENT)).agentProfile(agent1).build();
		when(userRepo.findById("A1")).thenReturn(Mono.just(agent));
		StepVerifier.create(service.getAgentStats("A1")).assertNext(stats -> {
			assertEquals("A1", stats.getAgentId());
			assertEquals(AGENT_LEVEL.L2, stats.getAgentLevel());
			assertEquals(4, stats.getCurrentAssignments());
			assertEquals(6, stats.getResolvedCount());
			assertEquals(0.6, stats.getResolutionRate());
		}).verifyComplete();
	}

	@Test
	void register_success() {
		User user = User.builder().id("U123456").email("new@example.com").username("newuser").password("raw")
				.roles(Set.of(ROLE.USER)).build();

		when(userRepo.findByEmail("new@example.com")).thenReturn(Mono.empty());
		when(userRepo.findByUsername("newuser")).thenReturn(Mono.empty());
		when(passwordEncoder.encode("raw")).thenReturn("encoded");
		when(userRepo.save(any(User.class))).thenReturn(Mono.just(user));

		StepVerifier.create(service.register(user)).assertNext(resp -> {
			assertTrue(resp.isSuccess());
			assertEquals("User created successfully", resp.getMessage());
		}).verifyComplete();
	}

	@Test
	void incrementAssignments_agentNotFound() {
		when(userRepo.findById("A1")).thenReturn(Mono.empty());
		StepVerifier.create(service.incrementAssignments("A1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Agent not found")).verify();
	}

	@Test
	void getUserById_notFound() {
		when(userRepo.findById("U1")).thenReturn(Mono.empty());
		StepVerifier.create(service.getUserById("U1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("User not found")).verify();
	}

	@Test
	void resetPassword_invalidToken() {
		when(userRepo.findByResetToken("bad")).thenReturn(Mono.empty());
		StepVerifier.create(service.resetPassword("bad", "new"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Invalid or expired token")).verify();
	}

	@Test
	void decrementAssignments_success() {
		AgentProfile profile = AgentProfile.builder().currentAssignments(3).resolvedCount(5).agentLevel(AGENT_LEVEL.L1)
				.build();
		User agent = User.builder().id("A1").roles(Set.of(ROLE.AGENT)).agentProfile(profile).build();
		when(userRepo.findById("A1")).thenReturn(Mono.just(agent));
		when(userRepo.save(any(User.class))).thenReturn(Mono.just(agent));
		StepVerifier.create(service.decrementAssignments("A1"))
				.assertNext(saved -> assertEquals(2, saved.getAgentProfile().getCurrentAssignments())).verifyComplete();
	}

	@Test
	void decrementAssignments_agentNotFound() {
		when(userRepo.findById("A1")).thenReturn(Mono.empty());
		StepVerifier.create(service.decrementAssignments("A1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Agent not found")).verify();
	}

	@Test
	void incrementResolvedCount_success() {
		AgentProfile profile = AgentProfile.builder().currentAssignments(2).resolvedCount(1).agentLevel(AGENT_LEVEL.L1)
				.build();
		User agent = User.builder().id("A1").roles(Set.of(ROLE.AGENT)).agentProfile(profile).build();
		when(userRepo.findById("A1")).thenReturn(Mono.just(agent));
		when(userRepo.save(any(User.class))).thenReturn(Mono.just(agent));
		StepVerifier.create(service.incrementResolvedCount("A1"))
				.assertNext(saved -> assertEquals(2, saved.getAgentProfile().getResolvedCount())).verifyComplete();
	}

	@Test
	void incrementResolvedCount_agentNotFound() {
		when(userRepo.findById("A1")).thenReturn(Mono.empty());
		StepVerifier.create(service.incrementResolvedCount("A1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Agent not found")).verify();
	}

	@Test
	void getUserByEmail_success() {
		User user = User.builder().id("U1").email("e@x.com").username("u1").roles(Set.of(ROLE.USER)).enabled(true)
				.build();
		when(userRepo.findByEmail("e@x.com")).thenReturn(Mono.just(user));
		StepVerifier.create(service.getUserByEmail("e@x.com"))
				.assertNext(resp -> assertEquals("u1", resp.getUsername())).verifyComplete();
	}

	@Test
	void getUserByEmail_notFound() {
		when(userRepo.findByEmail("e@x.com")).thenReturn(Mono.empty());
		StepVerifier.create(service.getUserByEmail("e@x.com"))
				.expectErrorMatches(ex -> ex.getMessage().equals("User not found")).verify();
	}

	@Test
	void getUserEmail_success() {
		User user = User.builder().id("U1").email("e@x.com").build();
		when(userRepo.findById("U1")).thenReturn(Mono.just(user));
		StepVerifier.create(service.getUserEmail("U1")).assertNext(email -> assertEquals("e@x.com", email))
				.verifyComplete();
	}

	@Test
	void getUserEmail_notFound() {
		when(userRepo.findById("U1")).thenReturn(Mono.empty());
		StepVerifier.create(service.getUserEmail("U1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("User not found")).verify();
	}

	@Test
	void updateUserById_success() {
		User existing = User.builder().id("U1").username("old").email("old@x.com").roles(Set.of(ROLE.USER)).build();
		AuthResponse update = new AuthResponse("U1", "USR-U1", "new@x.com", "newuser", true, Set.of(ROLE.USER), null);
		when(userRepo.findById("U1")).thenReturn(Mono.just(existing));
		when(userRepo.findByUsername("newuser")).thenReturn(Mono.empty());
		when(userRepo.save(any(User.class))).thenReturn(Mono.just(existing));
		StepVerifier.create(service.updateUserById("U1", update))
				.assertNext(resp -> assertEquals("User updated successfully", resp.getMessage())).verifyComplete();
	}

	@Test
	void updateUserById_userNotFound() {
		AuthResponse update = new AuthResponse("U1", "USR-U1", "new@x.com", "newuser", true, Set.of(ROLE.USER), null);
		when(userRepo.findById("U1")).thenReturn(Mono.empty());
		StepVerifier.create(service.updateUserById("U1", update))
				.expectErrorMatches(ex -> ex.getMessage().equals("User not found")).verify();
	}

	@Test
	void updateUserById_usernameConflict() {
		User existing = User.builder().id("U1").username("old").build();
		User conflict = User.builder().id("U2").username("newuser").build();
		AuthResponse update = new AuthResponse("U1", "USR-U1", "new@x.com", "newuser", true, Set.of(ROLE.USER), null);
		when(userRepo.findById("U1")).thenReturn(Mono.just(existing));
		when(userRepo.findByUsername("newuser")).thenReturn(Mono.just(conflict));
		StepVerifier.create(service.updateUserById("U1", update))
				.expectErrorMatches(ex -> ex.getMessage().equals("Username already exists")).verify();
	}

	@Test
	void changePassword_success() {
		User user = User.builder().id("U1").username("user").password("oldEncoded").email("e@x.com").build();
		when(userRepo.findByUsername("user")).thenReturn(Mono.just(user));
		when(passwordEncoder.matches("old", "oldEncoded")).thenReturn(true);
		when(passwordEncoder.encode("new")).thenReturn("newEncoded");
		when(userRepo.save(any(User.class))).thenReturn(Mono.just(user));
		StepVerifier.create(service.changePassword("user", "old", "new"))
				.assertNext(resp -> assertEquals("Password changed successfully", resp.getMessage())).verifyComplete();
	}

	@Test
	void changePassword_userNotFound() {
		when(userRepo.findByUsername("user")).thenReturn(Mono.empty());
		StepVerifier.create(service.changePassword("user", "old", "new"))
				.expectErrorMatches(ex -> ex.getMessage().equals("User not found")).verify();
	}

	@Test
	void changePassword_invalidCurrentPassword() {
		User user = User.builder().id("U1").username("user").password("oldEncoded").build();
		when(userRepo.findByUsername("user")).thenReturn(Mono.just(user));
		when(passwordEncoder.matches("wrong", "oldEncoded")).thenReturn(false);
		StepVerifier.create(service.changePassword("user", "wrong", "new"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Invalid current password")).verify();
	}

	@Test
	void deleteUser_success() {
		User user = User.builder().id("U1").enabled(true).build();
		when(userRepo.findById("U1")).thenReturn(Mono.just(user));
		when(userRepo.save(any(User.class))).thenReturn(Mono.just(user));
		StepVerifier.create(service.deleteUserById("U1"))
				.assertNext(resp -> assertEquals("User disabled successfully", resp.getMessage())).verifyComplete();
	}

	@Test
	void deleteUser_userNotFound() {
		when(userRepo.findById("U1")).thenReturn(Mono.empty());
		StepVerifier.create(service.deleteUserById("U1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("User not found")).verify();
	}

	@Test
	void deleteUser_alreadyDisabled() {
		User user = User.builder().id("U1").enabled(false).build();
		when(userRepo.findById("U1")).thenReturn(Mono.just(user));
		StepVerifier.create(service.deleteUserById("U1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("User already disabled")).verify();
	}

	@Test
	void getAgentStats_notAgent() {
		AgentProfile agent = AgentProfile.builder().build();
		User user = User.builder().id("U1").roles(Set.of(ROLE.USER)).agentProfile(agent).build();
		when(userRepo.findById("U1")).thenReturn(Mono.just(user));
		StepVerifier.create(service.getAgentStats("U1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("User is not an agent")).verify();
	}

	@Test
	void getAgentStats_userNotFound() {
		when(userRepo.findById("U1")).thenReturn(Mono.empty());

		StepVerifier.create(service.getAgentStats("U1")).verifyComplete();
	}

}
