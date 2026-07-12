package com.traction.service;

import com.traction.dto.request.LoginRequest;
import com.traction.dto.request.RegisterRequest;
import com.traction.dto.response.AuthResponse;
import com.traction.entity.Role;
import com.traction.entity.User;
import com.traction.exception.DuplicateResourceException;
import com.traction.exception.UnauthorizedException;
import com.traction.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private AuthService authService;

	@Test
	void shouldRegisterUserSuccessfully() {
		RegisterRequest request = new RegisterRequest("newuser", "new@test.com", "password123");

		when(userRepository.existsByUsername("newuser")).thenReturn(false);
		when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("hashed");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(jwtTokenProvider.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

		AuthResponse response = authService.register(request);

		assertThat(response.getToken()).isEqualTo("jwt-token");
	}

	@Test
	void shouldEncodePasswordNotStoreRaw() {
		RegisterRequest request = new RegisterRequest("newuser", "new@test.com", "password123");

		when(userRepository.existsByUsername("newuser")).thenReturn(false);
		when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("hashed");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(jwtTokenProvider.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		authService.register(request);

		verify(userRepository).save(userCaptor.capture());
		assertThat(userCaptor.getValue().getPassword()).isEqualTo("hashed");
	}

	@Test
	void shouldThrowDuplicateWhenUsernameExists() {
		RegisterRequest request = new RegisterRequest("existing", "new@test.com", "password123");

		when(userRepository.existsByUsername("existing")).thenReturn(true);

		assertThatThrownBy(() -> authService.register(request))
				.isInstanceOf(DuplicateResourceException.class);
	}

	@Test
	void shouldThrowDuplicateWhenEmailExists() {
		RegisterRequest request = new RegisterRequest("newuser", "taken@test.com", "password123");

		when(userRepository.existsByUsername("newuser")).thenReturn(false);
		when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

		assertThatThrownBy(() -> authService.register(request))
				.isInstanceOf(DuplicateResourceException.class);
	}

	@Test
	void shouldReturnTokenOnValidLogin() {
		LoginRequest request = new LoginRequest("john", "raw");

		User user = User.builder()
				.username("john")
				.password("encoded")
				.role(Role.USER)
				.build();

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);
		when(jwtTokenProvider.generateToken(any(UserDetails.class))).thenReturn("token");

		AuthResponse response = authService.login(request);

		assertThat(response.getToken()).isEqualTo("token");
	}

	@Test
	void shouldThrowUnauthorizedOnWrongPassword() {
		LoginRequest request = new LoginRequest("john", "wrong");

		User user = User.builder()
				.username("john")
				.password("encoded")
				.role(Role.USER)
				.build();

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(UnauthorizedException.class);
	}

	@Test
	void shouldThrowWhenUserNotFoundOnLogin() {
		LoginRequest request = new LoginRequest("ghost", "password");

		when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(UnauthorizedException.class);
	}

}
