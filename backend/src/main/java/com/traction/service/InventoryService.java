package com.traction.service;

import com.traction.dto.response.PurchaseResponse;
import com.traction.dto.response.VehicleResponse;
import com.traction.entity.Purchase;
import com.traction.entity.User;
import com.traction.entity.Vehicle;
import com.traction.entity.VehicleStatus;
import com.traction.exception.InsufficientStockException;
import com.traction.exception.ResourceNotFoundException;
import com.traction.repository.PurchaseRepository;
import com.traction.repository.UserRepository;
import com.traction.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final VehicleRepository vehicleRepository;
    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;

    @Transactional
    public PurchaseResponse purchase(UUID vehicleId, String username) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new InsufficientStockException("Vehicle is not available for purchase");
        }

        vehicle.setStatus(VehicleStatus.SOLD);
        vehicleRepository.save(vehicle);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Purchase purchase = Purchase.builder()
                .user(user)
                .vehicle(vehicle)
                .totalPrice(vehicle.getPrice())
                .build();

        Purchase saved = purchaseRepository.save(purchase);

        return PurchaseResponse.builder()
                .purchaseId(saved.getId())
                .vehicleId(vehicle.getId())
                .vehicleName(vehicle.getMake() + " " + vehicle.getModel())
                .totalPrice(saved.getTotalPrice())
                .purchasedAt(saved.getPurchasedAt())
                .build();
    }

    @Transactional
    public VehicleResponse restock(UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        vehicle.setStatus(VehicleStatus.AVAILABLE);
        Vehicle saved = vehicleRepository.save(vehicle);

        return VehicleResponse.from(saved);
    }
}
