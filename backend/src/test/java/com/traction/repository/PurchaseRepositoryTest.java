package com.traction.repository;

import com.traction.entity.Purchase;
import com.traction.entity.Role;
import com.traction.entity.User;
import com.traction.entity.Vehicle;
import com.traction.entity.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class PurchaseRepositoryTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private PurchaseRepository purchaseRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private VehicleRepository vehicleRepository;

	@BeforeEach
	void setUp() {
		purchaseRepository.deleteAll();
		vehicleRepository.deleteAll();
		userRepository.deleteAll();
	}

	private User buildUser(String username) {
		User user = User.builder()
				.username(username)
				.email(username + "@test.com")
				.password("pass")
				.role(Role.USER)
				.build();
		return userRepository.saveAndFlush(user);
	}

	private Vehicle buildVehicle() {
		Vehicle vehicle = Vehicle.builder()
				.vin("VIN" + UUID.randomUUID().toString().substring(0, 10))
				.make("Toyota")
				.model("Camry")
				.year(2023)
				.price(new BigDecimal("25000.00"))
				.mileage(10)
				.color("Silver")
				.status(VehicleStatus.AVAILABLE)
				.build();
		return vehicleRepository.saveAndFlush(vehicle);
	}

	@Test
	void shouldSavePurchaseAndPersistFields() {
		User user = buildUser("buyer");
		Vehicle vehicle = buildVehicle();

		Purchase purchase = Purchase.builder()
				.user(user)
				.vehicle(vehicle)
				.totalPrice(new BigDecimal("25000.00"))
				.build();

		Purchase saved = purchaseRepository.saveAndFlush(purchase);

		assertThat(saved.getId()).isNotNull();
		assertThat(purchaseRepository.findById(saved.getId()))
				.isPresent()
				.get()
				.satisfies(p -> {
					assertThat(p.getUser().getId()).isEqualTo(user.getId());
					assertThat(p.getVehicle().getId()).isEqualTo(vehicle.getId());
					assertThat(p.getTotalPrice()).isEqualByComparingTo("25000.00");
				});
	}

	@Test
	void shouldFindPurchasesByUser() {
		User userA = buildUser("userA");
		User userB = buildUser("userB");
		Vehicle v1 = buildVehicle();
		Vehicle v2 = buildVehicle();
		Vehicle v3 = buildVehicle();

		Purchase p1 = Purchase.builder().user(userA).vehicle(v1).totalPrice(new BigDecimal("25000.00")).build();
		Purchase p2 = Purchase.builder().user(userA).vehicle(v2).totalPrice(new BigDecimal("25000.00")).build();
		Purchase p3 = Purchase.builder().user(userB).vehicle(v3).totalPrice(new BigDecimal("25000.00")).build();

		purchaseRepository.saveAndFlush(p1);
		purchaseRepository.saveAndFlush(p2);
		purchaseRepository.saveAndFlush(p3);

		List<Purchase> purchasesUserA = purchaseRepository.findByUserId(userA.getId());
		assertThat(purchasesUserA).hasSize(2);
	}

	@Test
	void shouldFindPurchasesByVehicle() {
		User user = buildUser("user");
		Vehicle v1 = buildVehicle();

		Purchase p1 = Purchase.builder().user(user).vehicle(v1).totalPrice(new BigDecimal("25000.00")).build();

		purchaseRepository.saveAndFlush(p1);

		List<Purchase> purchasesV1 = purchaseRepository.findByVehicleId(v1.getId());
		assertThat(purchasesV1).hasSize(1);
	}
}
