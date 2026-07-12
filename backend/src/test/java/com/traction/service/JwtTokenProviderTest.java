package com.traction.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

	private static final String SECRET = "testSecretKeyForJwtThatMustBe256BitsLongAbcdefghij";

	private JwtTokenProvider jwtTokenProvider;

	@BeforeEach
	void setUp() {
		jwtTokenProvider = new JwtTokenProvider(SECRET, 3600000L);
	}

	@Test
	void shouldGenerateNonNullToken() {
		UserDetails userDetails = User.withUsername("john")
				.password("pass")
				.roles("USER")
				.build();

		String token = jwtTokenProvider.generateToken(userDetails);

		assertThat(token).isNotNull().isNotEmpty();
	}

	@Test
	void shouldExtractCorrectUsername() {
		UserDetails userDetails = User.withUsername("john")
				.password("pass")
				.roles("USER")
				.build();

		String token = jwtTokenProvider.generateToken(userDetails);

		assertThat(jwtTokenProvider.extractUsername(token)).isEqualTo("john");
	}

	@Test
	void shouldValidateGoodToken() {
		UserDetails userDetails = User.withUsername("john")
				.password("pass")
				.roles("USER")
				.build();

		String token = jwtTokenProvider.generateToken(userDetails);

		assertThat(jwtTokenProvider.validateToken(token, userDetails)).isTrue();
	}

	@Test
	void shouldRejectExpiredToken() throws InterruptedException {
		JwtTokenProvider shortLivedProvider = new JwtTokenProvider(SECRET, 1L);

		UserDetails userDetails = User.withUsername("john")
				.password("pass")
				.roles("USER")
				.build();

		String token = shortLivedProvider.generateToken(userDetails);
		Thread.sleep(5);

		assertThat(shortLivedProvider.validateToken(token, userDetails)).isFalse();
	}

	@Test
	void shouldRejectTamperedToken() {
		UserDetails userDetails = User.withUsername("john")
				.password("pass")
				.roles("USER")
				.build();

		String token = jwtTokenProvider.generateToken(userDetails);
		String tamperedToken = token.substring(0, token.length() - 3) + "xxx";

		assertThat(jwtTokenProvider.validateToken(tamperedToken, userDetails)).isFalse();
	}

	@Test
	void shouldRejectWrongUserToken() {
		UserDetails alice = User.withUsername("alice")
				.password("pass")
				.roles("USER")
				.build();

		UserDetails bob = User.withUsername("bob")
				.password("pass")
				.roles("USER")
				.build();

		String token = jwtTokenProvider.generateToken(alice);

		assertThat(jwtTokenProvider.validateToken(token, bob)).isFalse();
	}

}
