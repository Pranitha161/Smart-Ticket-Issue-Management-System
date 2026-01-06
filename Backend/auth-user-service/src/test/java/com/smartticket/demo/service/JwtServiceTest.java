package com.smartticket.demo.service;

import com.smartticket.demo.entity.User;
import com.smartticket.demo.enums.ROLE;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

	private JwtService jwtService;

	@BeforeEach
	void setup() throws Exception {
		jwtService = new JwtService();

		// Inject private fields via reflection
		Field secretField = JwtService.class.getDeclaredField("secret");
		secretField.setAccessible(true);
		secretField.set(jwtService, "my-test-secret-key-my-test-secret-key"); // must be >= 32 chars

		Field expField = JwtService.class.getDeclaredField("expirationMinutes");
		expField.setAccessible(true);
		expField.set(jwtService, 60L);
	}

	@Test
	void generateToken_containsExpectedClaims() {
		User user = User.builder().id("U1").email("test@example.com").username("tester").roles(Set.of(ROLE.USER))
				.build();

		String token = jwtService.generateToken(user);
		assertNotNull(token);

		Claims claims = Jwts.parserBuilder()
				.setSigningKey(
						Keys.hmacShaKeyFor("my-test-secret-key-my-test-secret-key".getBytes(StandardCharsets.UTF_8)))
				.build().parseClaimsJws(token).getBody();

		assertEquals("U1", claims.getSubject());
		assertEquals("test@example.com", claims.get("email"));
		assertEquals("tester", claims.get("username"));
		assertTrue(((java.util.List<?>) claims.get("roles")).contains("USER"));

		assertNotNull(claims.getIssuedAt());
		assertNotNull(claims.getExpiration());
		assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
	}

	@Test
	void generateToken_invalidSecretThrows() {
		User user = User.builder().id("U1").email("test@example.com").username("tester").roles(Set.of(ROLE.USER))
				.build();

		String token = jwtService.generateToken(user);

		assertThrows(SignatureException.class, () -> {
			Jwts.parserBuilder()
					.setSigningKey(Keys
							.hmacShaKeyFor("wrong-secret-wrong-secret-wrong-secret".getBytes(StandardCharsets.UTF_8)))
					.build().parseClaimsJws(token);
		});
	}

	@Test
	void generateToken_withMultipleRoles() throws Exception {
		User user = User.builder().id("U2").email("multi@example.com").username("multiuser")
				.roles(Set.of(ROLE.USER, ROLE.AGENT)).build();

		String token = jwtService.generateToken(user);

		Claims claims = Jwts.parserBuilder()
				.setSigningKey(
						Keys.hmacShaKeyFor("my-test-secret-key-my-test-secret-key".getBytes(StandardCharsets.UTF_8)))
				.build().parseClaimsJws(token).getBody();

		assertTrue(((java.util.List<?>) claims.get("roles")).contains("USER"));
		assertTrue(((java.util.List<?>) claims.get("roles")).contains("AGENT"));
	}

	@Test
	void generateToken_expirationSetCorrectly() throws Exception {

		Field expField = JwtService.class.getDeclaredField("expirationMinutes");
		expField.setAccessible(true);
		expField.set(jwtService, 1L);

		User user = User.builder().id("U3").email("exp@example.com").username("expuser").roles(Set.of(ROLE.USER))
				.build();

		String token = jwtService.generateToken(user);

		Claims claims = Jwts.parserBuilder()
				.setSigningKey(
						Keys.hmacShaKeyFor("my-test-secret-key-my-test-secret-key".getBytes(StandardCharsets.UTF_8)))
				.build().parseClaimsJws(token).getBody();

		long diffMillis = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
		assertTrue(diffMillis <= 60_000 + 1000);
	}

	@Test
	void generateToken_immediatelyExpired() throws Exception {
		Field expField = JwtService.class.getDeclaredField("expirationMinutes");
		expField.setAccessible(true);
		expField.set(jwtService, 0L);

		User user = User.builder().id("U4").email("expired@example.com").username("expired").roles(Set.of(ROLE.USER))
				.build();

		String token = jwtService.generateToken(user);

		assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
			Jwts.parserBuilder()
					.setSigningKey(Keys
							.hmacShaKeyFor("my-test-secret-key-my-test-secret-key".getBytes(StandardCharsets.UTF_8)))
					.build().parseClaimsJws(token);
		});
	}

	@Test
	void generateToken_tamperedTokenFails() {
		User user = User.builder().id("U5").email("tamper@example.com").username("tamper").roles(Set.of(ROLE.USER))
				.build();

		String token = jwtService.generateToken(user);
		String tampered = token.substring(0, token.length() - 1);

		assertThrows(SignatureException.class, () -> {
			Jwts.parserBuilder()
					.setSigningKey(Keys
							.hmacShaKeyFor("my-test-secret-key-my-test-secret-key".getBytes(StandardCharsets.UTF_8)))
					.build().parseClaimsJws(tampered);
		});
	}

}
