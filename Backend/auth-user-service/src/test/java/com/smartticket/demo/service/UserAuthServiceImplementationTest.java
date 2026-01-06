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

import reactor.core.publisher.Flux;
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
		User existing = User.builder().id("U1").username("old").email("old@x.com").enabled(true)
				.roles(Set.of(ROLE.USER)).build();
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
		User existing = User.builder().id("U1").username("old").enabled(true).build();
		User conflict = User.builder().id("U2").username("newuser").enabled(true).build();
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

	@Test
	void requestPasswordReset_success() {
		User user = User.builder().id("U1").email("e@x.com").build();
		when(userRepo.findByEmail("e@x.com")).thenReturn(Mono.just(user));
		when(userRepo.save(any(User.class))).thenReturn(Mono.just(user));
		StepVerifier.create(service.requestPasswordReset("e@x.com"))
				.assertNext(resp -> assertEquals("Reset link sent to your email", resp.getMessage())).verifyComplete();
	}

	@Test
	void enableUser_success() {
		User user = User.builder().id("U1").enabled(false).build();
		when(userRepo.findById("U1")).thenReturn(Mono.just(user));
		when(userRepo.save(any(User.class))).thenReturn(Mono.just(user));
		StepVerifier.create(service.enableUserById("U1"))
				.assertNext(resp -> assertEquals("User enabled successfully", resp.getMessage())).verifyComplete();
	}

	@Test
	void getUsers_emptyList() {
		when(userRepo.findAll()).thenReturn(Flux.empty());
		StepVerifier.create(service.getUsers()).assertNext(list -> assertTrue(list.isEmpty())).verifyComplete();
	}

	@Test
	void register_agentMissingCategory() {
		AgentProfile profile = AgentProfile.builder().agentLevel(AGENT_LEVEL.L1).build();
		User user = User.builder().email("a@x.com").username("agent").roles(Set.of(ROLE.AGENT)).agentProfile(profile)
				.build();
		when(userRepo.findByEmail("a@x.com")).thenReturn(Mono.empty());
		when(userRepo.findByUsername("agent")).thenReturn(Mono.empty());
		StepVerifier.create(service.register(user))
				.expectErrorMatches(ex -> ex.getMessage().contains("Agent must have a specialization category"))
				.verify();
	}

	@Test
	void resetPassword_expiredToken() {
		User user = User.builder().id("U1").resetToken("token123").resetTokenExpiry(Instant.now().minusSeconds(60))
				.build();
		when(userRepo.findByResetToken("token123")).thenReturn(Mono.just(user));
		StepVerifier.create(service.resetPassword("token123", "newPass"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Invalid or expired token")).verify();
	}

	@Test
	void updateUserById_userDisabled() {
		User existing = User.builder().id("U1").enabled(false).build();
		AuthResponse update = new AuthResponse("U1", "USR-U1", "new@x.com", "newuser", true, Set.of(ROLE.USER), null);
		when(userRepo.findById("U1")).thenReturn(Mono.just(existing));
		StepVerifier.create(service.updateUserById("U1", update))
				.expectErrorMatches(ex -> ex.getMessage().equals("User is not active")).verify();
	}

	@Test
	void enableUser_userNotFound() {
		when(userRepo.findById("U1")).thenReturn(Mono.empty());
		StepVerifier.create(service.enableUserById("U1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("User not found")).verify();
	}

	@Test
	void enableUser_alreadyActive() {
		User user = User.builder().id("U1").enabled(true).build();
		when(userRepo.findById("U1")).thenReturn(Mono.just(user));
		StepVerifier.create(service.enableUserById("U1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("User is already active")).verify();
	}

	@Test
	void incrementEscalatedCount_agentNotFound() {
		when(userRepo.findById("A1")).thenReturn(Mono.empty());
		StepVerifier.create(service.incrementEscalatedCount("A1"))
				.expectErrorMatches(ex -> ex.getMessage().equals("Agent not found")).verify();
	}

	@Test
	void getUsers_success_multiple() {
		User u1 = User.builder().id("U1").username("u1").email("u1@x.com").roles(Set.of(ROLE.USER)).build();
		User u2 = User.builder().id("U2").username("u2").email("u2@x.com").roles(Set.of(ROLE.AGENT)).build();
		when(userRepo.findAll()).thenReturn(Flux.just(u1, u2));

		StepVerifier.create(service.getUsers()).assertNext(list -> {
			assertEquals(2, list.size());
			assertEquals("u1", list.get(0).getUsername());
			assertEquals("u2", list.get(1).getUsername());
		}).verifyComplete();
	}

//	@Test
//	void getUserStats_success() {
//	   
//	    when(userRepo.countTotalUsers()).thenReturn(Flux.just(new Document("totalUsers", 10)));
//	    when(userRepo.countActiveUsers()).thenReturn(Flux.just(new Document("activeUsers", 8)));
//	    when(userRepo.countUsersByRole()).thenReturn(Flux.just(
//	        new Document("_id", "AGENT").append("count", 5),
//	        new Document("_id", "USER").append("count", 3)
//	    ));
//
//	    StepVerifier.create(service.getUserStats())
//	        .assertNext(stats -> {
//	            assertEquals(10, stats.getTotalUsers());
//	            assertEquals(8, stats.getActiveUsers());
//	            assertEquals(5, stats.getAgents());
//	            assertEquals(3, stats.getUsers());
//	        })
//	        .verifyComplete();
//	}
	@Test
	void incrementEscalatedCount_success() {
		AgentProfile profile = AgentProfile.builder().escalatedCount(1).resolvedCount(0).agentLevel(AGENT_LEVEL.L1)
				.build();
		User agent = User.builder().id("A1").roles(Set.of(ROLE.AGENT)).agentProfile(profile).build();

		when(userRepo.findById("A1")).thenReturn(Mono.just(agent));
		when(userRepo.save(any(User.class))).thenReturn(Mono.just(agent));

		StepVerifier.create(service.incrementEscalatedCount("A1"))
				.assertNext(saved -> assertEquals(0, saved.getAgentProfile().getResolvedCount())).verifyComplete();
	}

	@Test
	void requestPasswordReset_userNotFound() {
		when(userRepo.findByEmail("missing@example.com")).thenReturn(Mono.empty());
		StepVerifier.create(service.requestPasswordReset("missing@example.com"))
				.expectErrorMatches(ex -> ex.getMessage().equals("User not found")).verify();
	}

	@Test
	void getAgentStats_resolutionRateZero() {
		AgentProfile profile = AgentProfile.builder().currentAssignments(0).resolvedCount(0).agentLevel(AGENT_LEVEL.L1)
				.build();
		User agent = User.builder().id("A1").roles(Set.of(ROLE.AGENT)).agentProfile(profile).build();
		when(userRepo.findById("A1")).thenReturn(Mono.just(agent));

		StepVerifier.create(service.getAgentStats("A1"))
				.assertNext(stats -> assertEquals(0.0, stats.getResolutionRate())).verifyComplete();
	}

	@Test
	void getUserStats_noUsers() {
		when(userRepo.countTotalUsers()).thenReturn(Flux.empty());
		when(userRepo.countActiveUsers()).thenReturn(Flux.empty());
		when(userRepo.countUsersByRole()).thenReturn(Flux.empty());

		StepVerifier.create(service.getUserStats()).assertNext(stats -> {
			assertEquals(0, stats.getTotalUsers());
			assertEquals(0, stats.getActiveUsers());
		}).verifyComplete();
	}

	@Test
	void getAllAgentStats_noAgents() {
		User nonAgent = User.builder().id("U1").roles(Set.of(ROLE.USER)).build();
		when(userRepo.findAll()).thenReturn(Flux.just(nonAgent));

		StepVerifier.create(service.getAllAgentStats()).assertNext(list -> assertTrue(list.isEmpty())).verifyComplete();
	}

	@Test
	void getUserStats_success() {
		when(userRepo.countTotalUsers()).thenReturn(Flux.just(new org.bson.Document("totalUsers", 10)));
		when(userRepo.countActiveUsers()).thenReturn(Flux.just(new org.bson.Document("activeUsers", 8)));
		when(userRepo.countUsersByRole()).thenReturn(Flux.just(new org.bson.Document("_id", "AGENT").append("count", 5),
				new org.bson.Document("_id", "USER").append("count", 3),
				new org.bson.Document("_id", "ADMIN").append("count", 1)));

		StepVerifier.create(service.getUserStats()).assertNext(stats -> {
			assertEquals(10, stats.getTotalUsers());
			assertEquals(8, stats.getActiveUsers());

			assertEquals(1, stats.getAdmins());
		}).verifyComplete();
	}

	@Test
	void getAllAgentStats_success() {
		AgentProfile p1 = AgentProfile.builder().currentAssignments(2).resolvedCount(3).agentLevel(AGENT_LEVEL.L1)
				.build();
		AgentProfile p2 = AgentProfile.builder().currentAssignments(0).resolvedCount(0).agentLevel(AGENT_LEVEL.L2)
				.build();
		User a1 = User.builder().id("A1").roles(Set.of(ROLE.AGENT)).agentProfile(p1).build();
		User a2 = User.builder().id("A2").roles(Set.of(ROLE.AGENT)).agentProfile(p2).build();

		when(userRepo.findAll()).thenReturn(Flux.just(a1, a2));

		StepVerifier.create(service.getAllAgentStats()).assertNext(list -> {
			assertEquals(2, list.size());
			assertEquals("A1", list.get(0).getAgentId());
			assertEquals(0.6, list.get(0).getResolutionRate());
			assertEquals("A2", list.get(1).getAgentId());
			assertEquals(0.0, list.get(1).getResolutionRate());
		}).verifyComplete();
	}

	@Test
	void register_invalidCategoryId() {
		AgentProfile profile = AgentProfile.builder().agentLevel(AGENT_LEVEL.L1).categoryId("bad").build();
		User user = User.builder().email("a@x.com").username("agent").roles(Set.of(ROLE.AGENT)).agentProfile(profile)
				.build();
		when(userRepo.findByEmail("a@x.com")).thenReturn(Mono.empty());
		when(userRepo.findByUsername("agent")).thenReturn(Mono.empty());
		when(categoryClient.getCategoryById("bad")).thenThrow(new RuntimeException("Invalid category ID"));

		StepVerifier.create(service.register(user))
				.expectErrorMatches(ex -> ex.getMessage().equals("Invalid category ID")).verify();
	}

	@Test
	void getUserStats_noUsers_defaultsToZero() {
		when(userRepo.countTotalUsers()).thenReturn(Flux.empty());
		when(userRepo.countActiveUsers()).thenReturn(Flux.empty());
		when(userRepo.countUsersByRole()).thenReturn(Flux.empty());

		StepVerifier.create(service.getUserStats()).assertNext(stats -> {
			assertEquals(0, stats.getTotalUsers());
			assertEquals(0, stats.getActiveUsers());

			assertEquals(0, stats.getManagers());
			assertEquals(0, stats.getAdmins());
		}).verifyComplete();
	}

	@Test
	void getAllAgentStats_noAgents_returnsEmptyList() {
		User nonAgent = User.builder().id("U1").roles(Set.of(ROLE.USER)).agentProfile(
				AgentProfile.builder().agentLevel(AGENT_LEVEL.L1).currentAssignments(1).resolvedCount(1).build())
				.build();
		when(userRepo.findAll()).thenReturn(Flux.just(nonAgent));

		StepVerifier.create(service.getAllAgentStats()).assertNext(list -> assertTrue(list.isEmpty())).verifyComplete();
	}

	@Test
	void getAgentsByCategory_success() {
		AgentProfile p = AgentProfile.builder().agentLevel(AGENT_LEVEL.L1).categoryId("C1").build();
		User a = User.builder().id("A1").roles(Set.of(ROLE.AGENT)).agentProfile(p).build();
		when(userRepo.findByRolesContainingAndAgentProfileCategoryId("AGENT", "C1")).thenReturn(Flux.just(a));

		StepVerifier.create(service.getAgentsByCategory("C1")).assertNext(u -> {
			assertEquals("A1", u.getId());
			assertEquals(AGENT_LEVEL.L1, u.getAgentProfile().getAgentLevel());
		}).verifyComplete();
	}

	@Test
	void getAgentsByCategory_empty() {
		when(userRepo.findByRolesContainingAndAgentProfileCategoryId("AGENT", "C1")).thenReturn(Flux.empty());

		StepVerifier.create(service.getAgentsByCategory("C1")).verifyComplete();
	}

}
