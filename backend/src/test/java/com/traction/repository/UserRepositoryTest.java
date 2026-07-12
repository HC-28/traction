package com.traction.repository;

import com.traction.entity.Role;
import com.traction.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class UserRepositoryTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
	}

	@Test
	void shouldSaveUserAndFindByUsername() {
		User user = User.builder()
				.username("john")
				.email("john@test.com")
				.password("pass")
				.role(Role.USER)
				.build();

		userRepository.save(user);

		assertThat(userRepository.findByUsername("john"))
				.isPresent()
				.get()
				.extracting(User::getUsername)
				.isEqualTo("john");
	}

	@Test
	void shouldReturnEmptyForUnknownUsername() {
		assertThat(userRepository.findByUsername("ghost")).isEmpty();
	}

	@Test
	void shouldReturnTrueWhenUsernameExists() {
		User user = User.builder()
				.username("alice")
				.email("alice@test.com")
				.password("pass")
				.role(Role.USER)
				.build();

		userRepository.save(user);

		assertThat(userRepository.existsByUsername("alice")).isTrue();
	}

	@Test
	void shouldReturnFalseWhenUsernameAbsent() {
		assertThat(userRepository.existsByUsername("nobody")).isFalse();
	}

	@Test
	void shouldThrowOnDuplicateUsername() {
		User first = User.builder()
				.username("same")
				.email("first@test.com")
				.password("pass")
				.role(Role.USER)
				.build();

		User second = User.builder()
				.username("same")
				.email("second@test.com")
				.password("pass")
				.role(Role.USER)
				.build();

		userRepository.save(first);

		assertThatThrownBy(() -> userRepository.save(second))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void shouldThrowOnDuplicateEmail() {
		User first = User.builder()
				.username("user1")
				.email("same@test.com")
				.password("pass")
				.role(Role.USER)
				.build();

		User second = User.builder()
				.username("user2")
				.email("same@test.com")
				.password("pass")
				.role(Role.USER)
				.build();

		userRepository.save(first);

		assertThatThrownBy(() -> userRepository.save(second))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

}
