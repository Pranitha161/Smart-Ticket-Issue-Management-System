package com.smartticket.demo.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

	@Test
	void passwordEncoderBeanIsBCrypt() {
		SecurityConfig config = new SecurityConfig();
		PasswordEncoder encoder = config.passwordEncoder();
		assertNotNull(encoder);
		String encoded = encoder.encode("secret");
		assertTrue(encoder.matches("secret", encoded));
	}

	@Test
	void jwtDecoderBeanCreatedWithSecret() {
		SecurityConfig config = new SecurityConfig();
		ReactiveJwtDecoder decoder = config.jwtDecoder("my-test-secret-key-my-test-secret-key");
		assertNotNull(decoder);
		assertEquals("org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder", decoder.getClass().getName());
	}

	@Test
	void jwtAuthenticationConverterBeanCreated() {
		SecurityConfig config = new SecurityConfig();
		Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> converter = config
				.jwtAuthenticationConverter();
		assertNotNull(converter);
		assertEquals(
				"org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter",
				converter.getClass().getName());
	}

	@Test
	void securityWebFilterChainBeanCreated() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				SecurityConfig.class)) {
			SecurityWebFilterChain chain = context.getBean(SecurityWebFilterChain.class);
			assertNotNull(chain);
		}
	}
}
