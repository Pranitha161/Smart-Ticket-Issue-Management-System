package com.smartticket.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.smartticket.demo.dto.AgentStatsDto;
import com.smartticket.demo.dto.UserStatsDto;
import com.smartticket.demo.entity.ApiResponse;
import com.smartticket.demo.entity.AuthResponse;
import com.smartticket.demo.entity.LoginRequest;
import com.smartticket.demo.entity.User;
import com.smartticket.demo.enums.AGENT_LEVEL;
import com.smartticket.demo.enums.ROLE;
import com.smartticket.demo.service.implementation.UserAuthServiceImplementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class UserAuthControllerTest {
	@Mock
	private UserAuthServiceImplementation userAuthService;
	@InjectMocks
	private UserAuthController controller;

	@Test
	void register_success() {
		User user = User.builder().id("U123456").email("new@example.com").username("newuser").roles(Set.of(ROLE.USER))
				.build();
		ApiResponse resp = new ApiResponse(true, "User created successfully");
		when(userAuthService.register(user)).thenReturn(Mono.just(resp));
		StepVerifier.create(controller.register(user)).assertNext(entity -> {
			assertEquals(HttpStatus.CREATED, entity.getStatusCode());
			assertTrue(entity.getBody().isSuccess());
		}).verifyComplete();
	}

	@Test
	void login_success() {
		LoginRequest req = new LoginRequest("e@x.com", "pass");
		ApiResponse resp = new ApiResponse(true, "jwt-token");
		when(userAuthService.login(req)).thenReturn(Mono.just(resp));
		StepVerifier.create(controller.login(req)).assertNext(entity -> {
			assertEquals(HttpStatus.OK, entity.getStatusCode());
			assertEquals("jwt-token", entity.getBody().getMessage());
		}).verifyComplete();
	}

	@Test
	void getEmail_success() {
		when(userAuthService.getUserEmail("U1")).thenReturn(Mono.just("e@x.com"));
		StepVerifier.create(controller.getEmail("U1")).assertNext(entity -> {
			assertEquals("e@x.com", entity.getBody());
		}).verifyComplete();
	}

	@Test
	void deleteUser_success() {
		ApiResponse resp = new ApiResponse(true, "User disabled successfully");
		when(userAuthService.deleteUserById("U1")).thenReturn(Mono.just(resp));
		StepVerifier.create(controller.deleteUserById("U1")).assertNext(entity -> {
			assertEquals(HttpStatus.OK, entity.getStatusCode());
			assertTrue(entity.getBody().isSuccess());
		}).verifyComplete();
	}

	@Test
	void requestPasswordReset_success() {
		ApiResponse resp = new ApiResponse(true, "Reset link sent");
		when(userAuthService.requestPasswordReset("e@x.com")).thenReturn(Mono.just(resp));
		StepVerifier.create(controller.requestPasswordReset("e@x.com"))
				.assertNext(entity -> assertTrue(entity.getBody().isSuccess())).verifyComplete();
	}

	@Test
	void resetPassword_success() {
		ApiResponse resp = new ApiResponse(true, "Password reset successful");
		when(userAuthService.resetPassword("token", "newPass")).thenReturn(Mono.just(resp));
		StepVerifier.create(controller.resetPassword("token", "newPass"))
				.assertNext(entity -> assertEquals("Password reset successful", entity.getBody().getMessage()))
				.verifyComplete();
	}

	@Test
	void changePassword_success() {
		ApiResponse resp = new ApiResponse(true, "Password changed successfully");
		when(userAuthService.changePassword("user", "old", "new")).thenReturn(Mono.just(resp));
		StepVerifier.create(controller.changePassword("user", "old", "new"))
				.assertNext(entity -> assertEquals("Password changed successfully", entity.getBody().getMessage()))
				.verifyComplete();
	}

	@Test
	void getUsers_success() {
		AuthResponse ar = new AuthResponse("U1", "USR-U1", "e@x.com", "u1", true, Set.of(ROLE.USER), null);
		when(userAuthService.getUsers()).thenReturn(Mono.just(List.of(ar)));
		StepVerifier.create(controller.getUsers()).assertNext(entity -> assertEquals(1, entity.getBody().size()))
				.verifyComplete();
	}

	@Test
	void getUserById_success() {
		AuthResponse ar = new AuthResponse("U1", "USR-U1", "e@x.com", "u1", true, Set.of(ROLE.USER), null);
		when(userAuthService.getUserById("U1")).thenReturn(Mono.just(ar));
		StepVerifier.create(controller.getUserById("U1"))
				.assertNext(entity -> assertEquals("u1", entity.getBody().getUsername())).verifyComplete();
	}

	@Test
	void getUserByEmail_success() {
		AuthResponse ar = new AuthResponse("U1", "USR-U1", "e@x.com", "u1", true, Set.of(ROLE.USER), null);
		when(userAuthService.getUserByEmail("e@x.com")).thenReturn(Mono.just(ar));
		StepVerifier.create(controller.getUserByEmail("e@x.com"))
				.assertNext(entity -> assertEquals("U1", entity.getBody().getId())).verifyComplete();
	}

	@Test
	void updateUserById_success() {
		AuthResponse ar = new AuthResponse("U1", "USR-U1", "e@x.com", "u1", true, Set.of(ROLE.USER), null);
		ApiResponse resp = new ApiResponse(true, "User updated successfully");
		when(userAuthService.updateUserById("U1", ar)).thenReturn(Mono.just(resp));
		StepVerifier.create(controller.updateUserById("U1", ar))
				.assertNext(entity -> assertEquals("User updated successfully", entity.getBody().getMessage()))
				.verifyComplete();
	}

	@Test
	void create_success() {
		User agent = User.builder().id("A1").username("agent").email("a@x.com").roles(Set.of(ROLE.AGENT)).build();
		ApiResponse resp = new ApiResponse(true, "Agent created");
		when(userAuthService.register(agent)).thenReturn(Mono.just(resp));
		StepVerifier.create(controller.create(agent))
				.assertNext(entity -> assertEquals("Agent created", entity.getBody().getMessage())).verifyComplete();
	}

	@Test
	void incrementAssignments_success() {
		User agent = User.builder().id("A1").username("agent").build();
		when(userAuthService.incrementAssignments("A1")).thenReturn(Mono.just(agent));
		StepVerifier.create(controller.incrementAssignments("A1"))
				.assertNext(entity -> assertEquals("Agent assignment count incremented", entity.getBody().getMessage()))
				.verifyComplete();
	}

	@Test
	void decrementAssignments_success() {
		User agent = User.builder().id("A1").username("agent").build();
		when(userAuthService.decrementAssignments("A1")).thenReturn(Mono.just(agent));
		StepVerifier.create(controller.decrementAssignments("A1"))
				.assertNext(entity -> assertEquals("Agent assignment count decremented", entity.getBody().getMessage()))
				.verifyComplete();
	}

	@Test
	void incrementResolvedCount_success() {
		User agent = User.builder().id("A1").username("agent").build();
		when(userAuthService.incrementResolvedCount("A1")).thenReturn(Mono.just(agent));
		StepVerifier.create(controller.incrementResolvedCount("A1"))
				.assertNext(entity -> assertEquals("Agent resolved count incremented", entity.getBody().getMessage()))
				.verifyComplete();
	}

	@Test
	void getUserStats_success() {
		UserStatsDto stats = new UserStatsDto(10, 8, 5, 3, 1, 1);
		when(userAuthService.getUserStats()).thenReturn(Mono.just(stats));
		StepVerifier.create(controller.getUserStats())
				.assertNext(entity -> assertEquals(10, entity.getBody().getTotalUsers())).verifyComplete();
	}

	@Test
	void debugAuth_noAuth() {
		StepVerifier.create(controller.debugAuth(null))
				.assertNext(entity -> assertEquals("No authentication found", entity.getBody())).verifyComplete();
	}

	@Test
	void getAgentsByCategory_success() {
		User agent = User.builder().id("A1").username("agent1").email("a1@x.com").agentProfile(null).build();
		when(userAuthService.getAgentsByCategory("C1")).thenReturn(Flux.just(agent));
		StepVerifier.create(controller.getAgentsByCategory("C1")).assertNext(dto -> {
			assertEquals("A1", dto.getId());
			assertEquals("agent1", dto.getUsername());
			assertEquals("a1@x.com", dto.getEmail());
		}).verifyComplete();
	}

	@Test
	void getAgentStats_success() {
		AgentStatsDto stats = new AgentStatsDto("A1", AGENT_LEVEL.L1, 4, 6, 0.6);
		when(userAuthService.getAgentStats("A1")).thenReturn(Mono.just(stats));
		StepVerifier.create(controller.getAgentStats("A1")).assertNext(dto -> {
			assertEquals("A1", dto.getAgentId());
			assertEquals(AGENT_LEVEL.L1, dto.getAgentLevel());
			assertEquals(4, dto.getCurrentAssignments());
			assertEquals(6, dto.getResolvedCount());
			assertEquals(0.6, dto.getResolutionRate());
		}).verifyComplete();
	}

	@Test
	void getAllAgentStats_success() {
		AgentStatsDto stats1 = new AgentStatsDto("A1", AGENT_LEVEL.L1, 2, 3, 0.6);
		AgentStatsDto stats2 = new AgentStatsDto("A2", AGENT_LEVEL.L2, 5, 10, 0.67);
		when(userAuthService.getAllAgentStats()).thenReturn(Mono.just(List.of(stats1, stats2)));
		StepVerifier.create(controller.getAllAgentStats()).assertNext(list -> {
			assertEquals(2, list.size());
			assertEquals("A1", list.get(0).getAgentId());
			assertEquals("A2", list.get(1).getAgentId());
		}).verifyComplete();
	}

}
