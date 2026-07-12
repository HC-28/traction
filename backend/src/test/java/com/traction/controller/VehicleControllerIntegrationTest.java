package com.traction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traction.dto.request.LoginRequest;
import com.traction.dto.request.VehicleRequest;
import com.traction.entity.Role;
import com.traction.entity.Vehicle;
import com.traction.entity.VehicleStatus;
import com.traction.repository.UserRepository;
import com.traction.repository.VehicleRepository;
import com.traction.service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class VehicleControllerIntegrationTest {

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

	/**
	 * MockBean replaces the real CloudinaryService in the Spring context,
	 * preventing actual HTTP calls to Cloudinary during integration tests.
	 */
	@MockBean
	private CloudinaryService cloudinaryService;

	private String adminToken;
	private String userToken;

	@BeforeEach
	void setUp() throws Exception {
		vehicleRepository.deleteAll();
		userRepository.deleteAll();

		adminToken = obtainToken("admin_user", "admin@traction.com", "password123", Role.ADMIN);
		userToken  = obtainToken("regular_user", "user@traction.com",  "password123", Role.USER);
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// POST /api/vehicles  — Create
	// ─────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("POST /api/vehicles")
	class CreateVehicleTests {

		@Test
		@DisplayName("ADMIN can create a vehicle and receives 201 with payload")
		void adminCanCreateVehicle() throws Exception {
			VehicleRequest request = buildRequest("1HGBH41JXMN109186");

			mockMvc.perform(post("/api/vehicles")
							.header("Authorization", "Bearer " + adminToken)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.data.id").isNotEmpty())
					.andExpect(jsonPath("$.data.make").value("Toyota"))
					.andExpect(jsonPath("$.data.vin").value("1HGBH41JXMN109186"))
					.andExpect(jsonPath("$.data.status").value("AVAILABLE"));
		}

		@Test
		@DisplayName("USER (non-admin) receives 403 when creating a vehicle")
		void userCannotCreateVehicle() throws Exception {
			VehicleRequest request = buildRequest("2HGBH41JXMN109186");

			mockMvc.perform(post("/api/vehicles")
							.header("Authorization", "Bearer " + userToken)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("unauthenticated request receives 401")
		void unauthenticatedCannotCreateVehicle() throws Exception {
			mockMvc.perform(post("/api/vehicles")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(buildRequest("3HGBH41JXMN109186"))))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("receives 400 when make is blank")
		void shouldReturn400WhenMakeBlank() throws Exception {
			VehicleRequest request = VehicleRequest.builder()
					.make("")
					.model("Camry")
					.year(2022)
					.color("White")
					.vin("BLANKMAKVN000001")
					.price(new BigDecimal("25000"))
					.mileage(0)
					.build();

			mockMvc.perform(post("/api/vehicles")
							.header("Authorization", "Bearer " + adminToken)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("receives 400 when price is zero or negative")
		void shouldReturn400WhenPriceNotPositive() throws Exception {
			VehicleRequest request = VehicleRequest.builder()
					.make("Toyota")
					.model("Camry")
					.year(2022)
					.color("White")
					.vin("ZEROPRICEVIN00001")
					.price(BigDecimal.ZERO)
					.mileage(0)
					.build();

			mockMvc.perform(post("/api/vehicles")
							.header("Authorization", "Bearer " + adminToken)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("receives 409 when VIN already exists")
		void shouldReturn409OnDuplicateVin() throws Exception {
			VehicleRequest request = buildRequest("DUPVIN00000000001");

			mockMvc.perform(post("/api/vehicles")
							.header("Authorization", "Bearer " + adminToken)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated());

			mockMvc.perform(post("/api/vehicles")
							.header("Authorization", "Bearer " + adminToken)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isConflict());
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// GET /api/vehicles/{id}  — Read by ID
	// ─────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("GET /api/vehicles/{id}")
	class GetVehicleByIdTests {

		@Test
		@DisplayName("any authenticated user can get vehicle by id")
		void authenticatedUserCanGetVehicle() throws Exception {
			Vehicle saved = vehicleRepository.save(buildVehicle("Honda", "Civic", "GETBYIDVIN0000001"));

			mockMvc.perform(get("/api/vehicles/{id}", saved.getId())
							.header("Authorization", "Bearer " + userToken))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.id").value(saved.getId().toString()))
					.andExpect(jsonPath("$.data.make").value("Honda"));
		}

		@Test
		@DisplayName("returns 404 for unknown id")
		void returns404ForUnknownId() throws Exception {
			mockMvc.perform(get("/api/vehicles/{id}", UUID.randomUUID())
							.header("Authorization", "Bearer " + userToken))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("unauthenticated request returns 401")
		void unauthenticatedReturns401() throws Exception {
			mockMvc.perform(get("/api/vehicles/{id}", UUID.randomUUID()))
					.andExpect(status().isUnauthorized());
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// GET /api/vehicles  — Search / list
	// ─────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("GET /api/vehicles")
	class GetVehiclesListTests {

		@BeforeEach
		void seedVehicles() {
			vehicleRepository.save(buildVehicle("Toyota", "Camry",  "LISTVIN000000001"));
			vehicleRepository.save(buildVehicle("Toyota", "Corolla","LISTVIN000000002"));
			vehicleRepository.save(buildVehicle("Honda",  "Civic",  "LISTVIN000000003"));
		}

		@Test
		@DisplayName("returns paginated list of vehicles")
		void returnsPaginatedVehicles() throws Exception {
			mockMvc.perform(get("/api/vehicles")
							.header("Authorization", "Bearer " + userToken)
							.param("page", "0")
							.param("size", "10"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.content", hasSize(3)))
					.andExpect(jsonPath("$.data.totalElements").value(3));
		}

		@Test
		@DisplayName("filters by make query param")
		void filtersByMake() throws Exception {
			mockMvc.perform(get("/api/vehicles")
							.header("Authorization", "Bearer " + userToken)
							.param("make", "Toyota"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.content", hasSize(2)));
		}

		@Test
		@DisplayName("filters by status query param")
		void filtersByStatus() throws Exception {
			mockMvc.perform(get("/api/vehicles")
							.header("Authorization", "Bearer " + userToken)
							.param("status", "AVAILABLE"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.content", hasSize(3)));
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// PUT /api/vehicles/{id}  — Update
	// ─────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("PUT /api/vehicles/{id}")
	class UpdateVehicleTests {

		@Test
		@DisplayName("ADMIN can update a vehicle")
		void adminCanUpdateVehicle() throws Exception {
			Vehicle saved = vehicleRepository.save(buildVehicle("Toyota", "Camry", "UPDATEVIN00000001"));

			VehicleRequest update = VehicleRequest.builder()
					.make("Toyota")
					.model("Camry XSE")
					.year(2023)
					.color("Black")
					.vin("UPDATEVIN00000001")
					.price(new BigDecimal("32000.00"))
					.mileage(500)
					.build();

			mockMvc.perform(put("/api/vehicles/{id}", saved.getId())
							.header("Authorization", "Bearer " + adminToken)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(update)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.model").value("Camry XSE"));
		}

		@Test
		@DisplayName("USER (non-admin) receives 403 when updating a vehicle")
		void userCannotUpdateVehicle() throws Exception {
			Vehicle saved = vehicleRepository.save(buildVehicle("Toyota", "Camry", "UPDATEVIN00000002"));

			mockMvc.perform(put("/api/vehicles/{id}", saved.getId())
							.header("Authorization", "Bearer " + userToken)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(buildRequest("UPDATEVIN00000002"))))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("returns 404 when updating non-existent vehicle")
		void returns404WhenVehicleNotFound() throws Exception {
			mockMvc.perform(put("/api/vehicles/{id}", UUID.randomUUID())
							.header("Authorization", "Bearer " + adminToken)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(buildRequest("NOTFOUNDVIN000001"))))
					.andExpect(status().isNotFound());
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// PATCH /api/vehicles/{id}/status  — Status Update
	// ─────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("PATCH /api/vehicles/{id}/status")
	class UpdateVehicleStatusTests {

		@Test
		@DisplayName("ADMIN can mark vehicle as SOLD")
		void adminCanMarkVehicleAsSold() throws Exception {
			Vehicle saved = vehicleRepository.save(buildVehicle("Ford", "Mustang", "SOLDVIN000000001"));

			mockMvc.perform(patch("/api/vehicles/{id}/status", saved.getId())
							.header("Authorization", "Bearer " + adminToken)
							.param("status", "SOLD"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.status").value("SOLD"));
		}

		@Test
		@DisplayName("returns 400 on invalid status value")
		void returns400OnInvalidStatus() throws Exception {
			Vehicle saved = vehicleRepository.save(buildVehicle("Ford", "Mustang", "BADSTATUSVIN00001"));

			mockMvc.perform(patch("/api/vehicles/{id}/status", saved.getId())
							.header("Authorization", "Bearer " + adminToken)
							.param("status", "INVALID_STATUS"))
					.andExpect(status().isBadRequest());
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// DELETE /api/vehicles/{id}  — Delete
	// ─────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("DELETE /api/vehicles/{id}")
	class DeleteVehicleTests {

		@Test
		@DisplayName("ADMIN can delete a vehicle and receives 204")
		void adminCanDeleteVehicle() throws Exception {
			Vehicle saved = vehicleRepository.save(buildVehicle("BMW", "X5", "DELETEVIN00000001"));

			mockMvc.perform(delete("/api/vehicles/{id}", saved.getId())
							.header("Authorization", "Bearer " + adminToken))
					.andExpect(status().isNoContent());
		}

		@Test
		@DisplayName("USER (non-admin) receives 403 when deleting a vehicle")
		void userCannotDeleteVehicle() throws Exception {
			Vehicle saved = vehicleRepository.save(buildVehicle("BMW", "X5", "DELETEVIN00000002"));

			mockMvc.perform(delete("/api/vehicles/{id}", saved.getId())
							.header("Authorization", "Bearer " + userToken))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("returns 404 when deleting non-existent vehicle")
		void returns404WhenDeletingNonExistentVehicle() throws Exception {
			mockMvc.perform(delete("/api/vehicles/{id}", UUID.randomUUID())
							.header("Authorization", "Bearer " + adminToken))
					.andExpect(status().isNotFound());
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────────
	// POST /api/vehicles/{id}/image  — Cloudinary Upload
	// ─────────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("POST /api/vehicles/{id}/image")
	class UploadVehicleImageTests {

		@Test
		@DisplayName("ADMIN can upload vehicle image and receives Cloudinary URL in response")
		void adminCanUploadImage() throws Exception {
			Vehicle saved = vehicleRepository.save(buildVehicle("Toyota", "Camry", "IMGVIN000000001"));
			String fakeUrl = "https://res.cloudinary.com/traction/vehicles/test.jpg";

			org.mockito.Mockito.when(cloudinaryService.uploadImage(org.mockito.ArgumentMatchers.any()))
					.thenReturn(fakeUrl);

			MockMultipartFile file = new MockMultipartFile(
					"file", "car.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image-bytes".getBytes());

			mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
					.multipart("/api/vehicles/{id}/image", saved.getId())
					.file(file)
					.header("Authorization", "Bearer " + adminToken))
					.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
					.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.imageUrl").value(fakeUrl));
		}

		@Test
		@DisplayName("USER receives 403 when attempting to upload a vehicle image")
		void userCannotUploadImage() throws Exception {
			Vehicle saved = vehicleRepository.save(buildVehicle("Honda", "Civic", "IMGVIN000000002"));

			MockMultipartFile file = new MockMultipartFile(
					"file", "car.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image-bytes".getBytes());

			mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
					.multipart("/api/vehicles/{id}/image", saved.getId())
					.file(file)
					.header("Authorization", "Bearer " + userToken))
					.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// Helpers
	// ─────────────────────────────────────────────────────────────────────────────

	/**
	 * Registers a user with the given role directly (bypassing the auth endpoint which
	 * only creates USER-role accounts), then logs in to obtain a JWT token.
	 */
	private String obtainToken(String username, String email, String password, Role role) throws Exception {
		// Register through the auth endpoint first (creates USER role).
		// For ADMIN we directly persist the user via repository so we can set the role.
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

	private VehicleRequest buildRequest(String vin) {
		return VehicleRequest.builder()
				.make("Toyota")
				.model("Camry")
				.year(2022)
				.color("White")
				.vin(vin)
				.price(new BigDecimal("25000.00"))
				.mileage(0)
				.build();
	}

	private Vehicle buildVehicle(String make, String model, String vin) {
		return Vehicle.builder()
				.make(make)
				.model(model)
				.year(2022)
				.color("White")
				.vin(vin)
				.price(new BigDecimal("25000.00"))
				.mileage(0)
				.status(VehicleStatus.AVAILABLE)
				.build();
	}
}
