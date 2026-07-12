package com.traction.service;

import com.traction.dto.response.PurchaseResponse;
import com.traction.entity.Purchase;
import com.traction.entity.Role;
import com.traction.entity.User;
import com.traction.entity.Vehicle;
import com.traction.entity.VehicleStatus;
import com.traction.exception.InsufficientStockException;
import com.traction.exception.ResourceNotFoundException;
import com.traction.repository.PurchaseRepository;
import com.traction.repository.UserRepository;
import com.traction.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

	@Mock
	private VehicleRepository vehicleRepository;

	@Mock
	private PurchaseRepository purchaseRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private InventoryService inventoryService;

	private Vehicle buildVehicle(VehicleStatus status) {
		return Vehicle.builder()
				.id(UUID.randomUUID())
				.vin("VIN1234567")
				.make("Toyota")
				.model("Camry")
				.year(2023)
				.price(new BigDecimal("25000.00"))
				.mileage(10)
				.color("Silver")
				.status(status)
				.build();
	}

	private User buildUser() {
		return User.builder()
				.id(UUID.randomUUID())
				.username("buyer")
				.email("buyer@test.com")
				.password("pass")
				.role(Role.USER)
				.build();
	}

	@Test
	void shouldMarkVehicleAsSoldOnPurchase() {
		Vehicle vehicle = buildVehicle(VehicleStatus.AVAILABLE);
		User user = buildUser();

		when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
		when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(user));
		when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PurchaseResponse response = inventoryService.purchase(vehicle.getId(), "buyer");

		ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
		verify(vehicleRepository).save(vehicleCaptor.capture());
		assertThat(vehicleCaptor.getValue().getStatus()).isEqualTo(VehicleStatus.SOLD);

		assertThat(response.getTotalPrice()).isEqualByComparingTo("25000.00");
	}

	@Test
	void shouldThrowInsufficientStockWhenVehicleAlreadySold() {
		Vehicle vehicle = buildVehicle(VehicleStatus.SOLD);

		when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

		assertThatThrownBy(() -> inventoryService.purchase(vehicle.getId(), "buyer"))
				.isInstanceOf(InsufficientStockException.class);
	}

	@Test
	void shouldThrowInsufficientStockWhenVehicleReserved() {
		Vehicle vehicle = buildVehicle(VehicleStatus.RESERVED);

		when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

		assertThatThrownBy(() -> inventoryService.purchase(vehicle.getId(), "buyer"))
				.isInstanceOf(InsufficientStockException.class);
	}

	@Test
	void shouldMarkVehicleAsAvailableOnRestock() {
		Vehicle vehicle = buildVehicle(VehicleStatus.SOLD);

		when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
		when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

		inventoryService.restock(vehicle.getId());

		ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
		verify(vehicleRepository).save(vehicleCaptor.capture());
		assertThat(vehicleCaptor.getValue().getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
	}

	@Test
	void shouldThrowWhenRestockingNonExistentVehicle() {
		UUID randomId = UUID.randomUUID();

		when(vehicleRepository.findById(randomId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> inventoryService.restock(randomId))
				.isInstanceOf(ResourceNotFoundException.class);
	}
}
