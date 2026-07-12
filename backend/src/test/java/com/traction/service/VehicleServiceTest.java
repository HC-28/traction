package com.traction.service;

import com.traction.dto.request.VehicleRequest;
import com.traction.dto.response.VehicleResponse;
import com.traction.entity.Vehicle;
import com.traction.entity.VehicleStatus;
import com.traction.exception.DuplicateResourceException;
import com.traction.exception.ResourceNotFoundException;
import com.traction.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

	@Mock
	private VehicleRepository vehicleRepository;

	@InjectMocks
	private VehicleService vehicleService;

	// ─────────────────────────────────────────────────────────────────────────────
	// Create
	// ─────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("Create Vehicle")
	class CreateVehicleTests {

		@Test
		@DisplayName("should create and return vehicle response on valid input")
		void shouldCreateVehicleSuccessfully() {
			VehicleRequest request = buildRequest("1HGBH41JXMN109186");

			when(vehicleRepository.existsByVin("1HGBH41JXMN109186")).thenReturn(false);
			when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> {
				Vehicle v = inv.getArgument(0);
				v.setId(UUID.randomUUID());
				return v;
			});

			VehicleResponse response = vehicleService.create(request);

			assertThat(response).isNotNull();
			assertThat(response.getMake()).isEqualTo("Toyota");
			assertThat(response.getVin()).isEqualTo("1HGBH41JXMN109186");
		}

		@Test
		@DisplayName("should default status to AVAILABLE on creation")
		void shouldDefaultStatusToAvailable() {
			VehicleRequest request = buildRequest("STATUSVIN00000001");

			when(vehicleRepository.existsByVin("STATUSVIN00000001")).thenReturn(false);
			when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

			ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
			vehicleService.create(request);

			verify(vehicleRepository).save(captor.capture());
			assertThat(captor.getValue().getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
		}

		@Test
		@DisplayName("should throw DuplicateResourceException when VIN already exists")
		void shouldThrowWhenVinExists() {
			VehicleRequest request = buildRequest("DUPVIN00000000001");

			when(vehicleRepository.existsByVin("DUPVIN00000000001")).thenReturn(true);

			assertThatThrownBy(() -> vehicleService.create(request))
					.isInstanceOf(DuplicateResourceException.class)
					.hasMessageContaining("VIN");
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// Read
	// ─────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("Read Vehicle")
	class ReadVehicleTests {

		@Test
		@DisplayName("should return vehicle response by id")
		void shouldReturnVehicleById() {
			UUID id = UUID.randomUUID();
			Vehicle vehicle = buildVehicle(id, "Toyota", "Camry", "READVIN000000001");

			when(vehicleRepository.findById(id)).thenReturn(Optional.of(vehicle));

			VehicleResponse response = vehicleService.findById(id);

			assertThat(response.getId()).isEqualTo(id);
			assertThat(response.getMake()).isEqualTo("Toyota");
		}

		@Test
		@DisplayName("should throw ResourceNotFoundException when vehicle not found")
		void shouldThrowWhenVehicleNotFound() {
			UUID id = UUID.randomUUID();

			when(vehicleRepository.findById(id)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> vehicleService.findById(id))
					.isInstanceOf(ResourceNotFoundException.class);
		}

		@Test
		@DisplayName("should return paginated list of vehicles")
		void shouldReturnPagedVehicles() {
			Vehicle vehicle = buildVehicle(UUID.randomUUID(), "Honda", "Civic", "PAGEVIN000000001");
			Page<Vehicle> page = new PageImpl<>(List.of(vehicle));

			when(vehicleRepository.search(any(), any(), any(), any(), any(), any(), any()))
					.thenReturn(page);

			Page<VehicleResponse> result = vehicleService.search(null, null, null, null, null, null, PageRequest.of(0, 10));

			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().get(0).getModel()).isEqualTo("Civic");
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// Update
	// ─────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("Update Vehicle")
	class UpdateVehicleTests {

		@Test
		@DisplayName("should update vehicle fields and return updated response")
		void shouldUpdateVehicleSuccessfully() {
			UUID id = UUID.randomUUID();
			Vehicle existing = buildVehicle(id, "Toyota", "Camry", "UPDATEVIN00000001");

			when(vehicleRepository.findById(id)).thenReturn(Optional.of(existing));
			when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

			VehicleRequest updateRequest = VehicleRequest.builder()
					.make("Toyota")
					.model("Camry XSE")
					.year(2023)
					.color("Black")
					.vin("UPDATEVIN00000001")
					.price(new BigDecimal("32000.00"))
					.mileage(500)
					.build();

			VehicleResponse response = vehicleService.update(id, updateRequest);

			assertThat(response.getModel()).isEqualTo("Camry XSE");
			assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("32000.00"));
		}

		@Test
		@DisplayName("should throw ResourceNotFoundException on update when vehicle not found")
		void shouldThrowOnUpdateWhenNotFound() {
			UUID id = UUID.randomUUID();

			when(vehicleRepository.findById(id)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> vehicleService.update(id, buildRequest("ANYVIN000000001")))
					.isInstanceOf(ResourceNotFoundException.class);
		}

		@Test
		@DisplayName("should throw DuplicateResourceException if updated VIN belongs to a different vehicle")
		void shouldThrowIfUpdatedVinBelongsToDifferentVehicle() {
			UUID id = UUID.randomUUID();
			UUID otherId = UUID.randomUUID();
			Vehicle existing  = buildVehicle(id,      "Toyota", "Camry", "ORIGINALVIN00001");
			Vehicle other     = buildVehicle(otherId, "Honda",  "Civic", "TAKENVIN0000001");

			when(vehicleRepository.findById(id)).thenReturn(Optional.of(existing));
			when(vehicleRepository.findByVin("TAKENVIN0000001")).thenReturn(Optional.of(other));

			VehicleRequest request = buildRequest("TAKENVIN0000001");

			assertThatThrownBy(() -> vehicleService.update(id, request))
					.isInstanceOf(DuplicateResourceException.class);
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// Delete
	// ─────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("Delete Vehicle")
	class DeleteVehicleTests {

		@Test
		@DisplayName("should delete vehicle by id")
		void shouldDeleteVehicle() {
			UUID id = UUID.randomUUID();
			Vehicle existing = buildVehicle(id, "Toyota", "Camry", "DELETEVIN00000001");

			when(vehicleRepository.findById(id)).thenReturn(Optional.of(existing));
			doNothing().when(vehicleRepository).delete(existing);

			vehicleService.delete(id);

			verify(vehicleRepository).delete(existing);
		}

		@Test
		@DisplayName("should throw ResourceNotFoundException on delete when vehicle not found")
		void shouldThrowOnDeleteWhenNotFound() {
			UUID id = UUID.randomUUID();

			when(vehicleRepository.findById(id)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> vehicleService.delete(id))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// Status transitions
	// ─────────────────────────────────────────────────────────────────────────────

	@Nested
	@DisplayName("Status Update")
	class StatusUpdateTests {

		@Test
		@DisplayName("should update vehicle status")
		void shouldUpdateVehicleStatus() {
			UUID id = UUID.randomUUID();
			Vehicle vehicle = buildVehicle(id, "Toyota", "Camry", "STATUSVIN00000001");

			when(vehicleRepository.findById(id)).thenReturn(Optional.of(vehicle));
			when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

			VehicleResponse response = vehicleService.updateStatus(id, VehicleStatus.SOLD);

			assertThat(response.getStatus()).isEqualTo(VehicleStatus.SOLD);
		}
	}

	// ─────────────────────────────────────────────────────────────────────────────
	// Helpers
	// ─────────────────────────────────────────────────────────────────────────────

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

	private Vehicle buildVehicle(UUID id, String make, String model, String vin) {
		return Vehicle.builder()
				.id(id)
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
