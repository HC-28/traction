package com.traction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traction.dto.request.LoginRequest;
import com.traction.entity.Role;
import com.traction.entity.Vehicle;
import com.traction.entity.VehicleStatus;
import com.traction.repository.UserRepository;
import com.traction.repository.VehicleRepository;
import com.traction.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class InventoryControllerIntegrationTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private VehicleRepository vehicleRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PurchaseRepository purchaseRepository;

	private String adminToken;
	private String userToken;

	@BeforeEach
	void setUp() throws Exception {
		purchaseRepository.deleteAll();
		vehicleRepository.deleteAll();
		userRepository.deleteAll();

		adminToken = obtainToken("admin_user", "admin@traction.com", "password123", Role.ADMIN);
		userToken  = obtainToken("regular_user", "user@traction.com",  "password123", Role.USER);
	}

	private String obtainToken(String username, String email, String password, Role role) throws Exception {
		com.traction.entity.User user = com.traction.entity.User.builder()
				.username(username)
				.email(email)
				.password(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password))
				.role(role)
				.build();
		userRepository.save(user);

		LoginRequest loginRequest = new LoginRequest(username, password);
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andReturn();

		String responseBody = result.getResponse().getContentAsString();
		return objectMapper.readTree(responseBody).at("/data/token").asText();
	}

	private Vehicle createVehicle(VehicleStatus status) {
		Vehicle vehicle = Vehicle.builder()
				.make("Toyota")
				.model("Camry")
				.year(2022)
				.color("White")
				.vin("VIN" + UUID.randomUUID().toString().substring(0, 10))
				.price(new BigDecimal("25000.00"))
				.mileage(0)
				.status(status)
				.build();
		return vehicleRepository.saveAndFlush(vehicle);
	}

	@Test
	void shouldReturn200WhenPurchasingAvailableVehicle() throws Exception {
		Vehicle vehicle = createVehicle(VehicleStatus.AVAILABLE);

		mockMvc.perform(post("/api/vehicles/{id}/purchase", vehicle.getId())
						.header("Authorization", "Bearer " + userToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.purchaseId").isNotEmpty())
				.andExpect(jsonPath("$.data.totalPrice").value(25000.00));

		Vehicle updated = vehicleRepository.findById(vehicle.getId()).orElseThrow();
		assertThat(updated.getStatus()).isEqualTo(VehicleStatus.SOLD);
	}

	@Test
	void shouldReturn400WhenPurchasingAlreadySoldVehicle() throws Exception {
		Vehicle vehicle = createVehicle(VehicleStatus.SOLD);

		mockMvc.perform(post("/api/vehicles/{id}/purchase", vehicle.getId())
						.header("Authorization", "Bearer " + userToken))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn200WhenAdminRestocks() throws Exception {
		Vehicle vehicle = createVehicle(VehicleStatus.SOLD);

		mockMvc.perform(post("/api/vehicles/{id}/restock", vehicle.getId())
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("AVAILABLE"));

		Vehicle updated = vehicleRepository.findById(vehicle.getId()).orElseThrow();
		assertThat(updated.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
	}

	@Test
	void shouldReturn403WhenUserTriesToRestock() throws Exception {
		Vehicle vehicle = createVehicle(VehicleStatus.SOLD);

		mockMvc.perform(post("/api/vehicles/{id}/restock", vehicle.getId())
						.header("Authorization", "Bearer " + userToken))
				.andExpect(status().isForbidden());
	}
}
