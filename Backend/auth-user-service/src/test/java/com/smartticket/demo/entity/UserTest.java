package com.smartticket.demo.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartticket.demo.enums.ROLE;

public class UserTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void testUserBuilderCreatesValidUser() {
		User user = User.builder().id("123").username("pranitha").password("securePass").email("test@example.com")
				.roles(Set.of(ROLE.ADMIN)).passwordLastChanged(LocalDateTime.now()).resetToken("reset123").enabled(true)
				.resetTokenExpiry(Instant.now().plusSeconds(3600)).build();

		assertThat(user.getUsername()).isEqualTo("pranitha");
		assertTrue(user.isEnabled());
		assertThat(user.getRoles()).contains(ROLE.ADMIN);
	}

	@Test
	void testValidationFailsForBlankUsername() {
		User user = User.builder().username("").password("securePass").email("test@example.com").build();

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
	}

	@Test
	void testValidationFailsForInvalidEmail() {
		User user = User.builder().username("validUser").password("securePass").email("invalid-email").build();

		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
	}
}
