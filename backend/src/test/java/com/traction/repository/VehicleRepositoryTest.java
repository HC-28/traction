package com.traction.repository;

import com.traction.entity.Vehicle;
import com.traction.entity.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class VehicleRepositoryTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private VehicleRepository vehicleRepository;

	@BeforeEach
	void setUp() {
		vehicleRepository.deleteAll();
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// Persistence basics
	// ─────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("Persistence")
	class PersistenceTests {

		@Test
		@DisplayName("should save vehicle and assign UUID")
		void shouldSaveVehicleAndAssignId() {
			Vehicle vehicle = buildVehicle("Toyota", "Camry", "1HGBH41JXMN109186");

			Vehicle saved = vehicleRepository.save(vehicle);

			assertThat(saved.getId()).isNotNull();
		}

		@Test
		@DisplayName("should set createdAt and updatedAt automatically")
		void shouldSetAuditTimestamps() {
			Vehicle saved = vehicleRepository.saveAndFlush(buildVehicle("Honda", "Civic", "2HGBH41JXMN109186"));

			assertThat(saved.getCreatedAt()).isNotNull();
			assertThat(saved.getUpdatedAt()).isNotNull();
		}

		@Test
		@DisplayName("should default status to AVAILABLE")
		void shouldDefaultStatusToAvailable() {
			Vehicle saved = vehicleRepository.save(buildVehicle("Ford", "Mustang", "3VWFE21C04M000001"));

			assertThat(saved.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
		}

		@Test
		@DisplayName("should find vehicle by id")
		void shouldFindById() {
			Vehicle saved = vehicleRepository.save(buildVehicle("BMW", "3 Series", "4VWFE21C04M000001"));

			Optional<Vehicle> found = vehicleRepository.findById(saved.getId());

			assertThat(found).isPresent();
			assertThat(found.get().getMake()).isEqualTo("BMW");
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// Uniqueness constraints
	// ─────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("Constraints")
	class ConstraintTests {

		@Test
		@DisplayName("should throw DataIntegrityViolationException on duplicate VIN")
		void shouldThrowOnDuplicateVin() {
			vehicleRepository.saveAndFlush(buildVehicle("Toyota", "Camry", "DUPVIN0000000001"));

			Vehicle duplicate = buildVehicle("Honda", "Civic", "DUPVIN0000000001");

			assertThatThrownBy(() -> vehicleRepository.saveAndFlush(duplicate))
					.isInstanceOf(DataIntegrityViolationException.class);
		}

		@Test
		@DisplayName("should return true when VIN already exists")
		void shouldReturnTrueWhenVinExists() {
			vehicleRepository.save(buildVehicle("Toyota", "Camry", "EXISTVIN000000001"));

			assertThat(vehicleRepository.existsByVin("EXISTVIN000000001")).isTrue();
		}

		@Test
		@DisplayName("should return false when VIN does not exist")
		void shouldReturnFalseWhenVinAbsent() {
			assertThat(vehicleRepository.existsByVin("NOVIN000000000001")).isFalse();
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// Dynamic search / filter (JPA Specification or equivalent)
	// ─────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("Dynamic Search")
	class SearchTests {

		@BeforeEach
		void seedData() {
			vehicleRepository.save(buildVehicle("Toyota", "Camry",  "SEARCH00000000001", 2021, new BigDecimal("25000"), VehicleStatus.AVAILABLE));
			vehicleRepository.save(buildVehicle("Toyota", "Corolla","SEARCH00000000002", 2022, new BigDecimal("22000"), VehicleStatus.AVAILABLE));
			vehicleRepository.save(buildVehicle("Honda",  "Civic",  "SEARCH00000000003", 2020, new BigDecimal("20000"), VehicleStatus.SOLD));
			vehicleRepository.save(buildVehicle("BMW",    "3 Series","SEARCH00000000004", 2023, new BigDecimal("45000"), VehicleStatus.RESERVED));
		}

		@Test
		@DisplayName("should filter vehicles by make")
		void shouldFilterByMake() {
			Pageable pageable = PageRequest.of(0, 10);
			Page<Vehicle> result = vehicleRepository.search("Toyota", null, null, null, null, null, pageable);

			assertThat(result.getContent()).hasSize(2);
			assertThat(result.getContent()).allMatch(v -> v.getMake().equals("Toyota"));
		}

		@Test
		@DisplayName("should filter vehicles by status")
		void shouldFilterByStatus() {
			Pageable pageable = PageRequest.of(0, 10);
			Page<Vehicle> result = vehicleRepository.search(null, null, null, null, null, "AVAILABLE", pageable);

			assertThat(result.getContent()).hasSize(2);
			assertThat(result.getContent()).allMatch(v -> v.getStatus() == VehicleStatus.AVAILABLE);
		}

		@Test
		@DisplayName("should filter vehicles by price range")
		void shouldFilterByPriceRange() {
			Pageable pageable = PageRequest.of(0, 10);
			Page<Vehicle> result = vehicleRepository.search(null, null, null, new BigDecimal("21000"), new BigDecimal("30000"), null, pageable);

			assertThat(result.getContent()).hasSize(2);
		}

		@Test
		@DisplayName("should filter vehicles by year")
		void shouldFilterByYear() {
			Pageable pageable = PageRequest.of(0, 10);
			Page<Vehicle> result = vehicleRepository.search(null, null, 2022, null, null, null, pageable);

			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().get(0).getModel()).isEqualTo("Corolla");
		}

		@Test
		@DisplayName("should return all vehicles when no filters provided")
		void shouldReturnAllWhenNoFilters() {
			Pageable pageable = PageRequest.of(0, 10);
			Page<Vehicle> result = vehicleRepository.search(null, null, null, null, null, null, pageable);

			assertThat(result.getTotalElements()).isEqualTo(4);
		}

		@Test
		@DisplayName("should support pagination")
		void shouldSupportPagination() {
			Pageable pageable = PageRequest.of(0, 2);
			Page<Vehicle> result = vehicleRepository.search(null, null, null, null, null, null, pageable);

			assertThat(result.getContent()).hasSize(2);
			assertThat(result.getTotalElements()).isEqualTo(4);
			assertThat(result.getTotalPages()).isEqualTo(2);
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// Helpers
	// ─────────────────────────────────────────────────────────────────────────────

	private Vehicle buildVehicle(String make, String model, String vin) {
		return buildVehicle(make, model, vin, 2022, new BigDecimal("25000.00"), VehicleStatus.AVAILABLE);
	}

	private Vehicle buildVehicle(String make, String model, String vin, int year, BigDecimal price, VehicleStatus status) {
		return Vehicle.builder()
				.make(make)
				.model(model)
				.year(year)
				.color("White")
				.vin(vin)
				.price(price)
				.mileage(0)
				.status(status)
				.build();
	}
}
