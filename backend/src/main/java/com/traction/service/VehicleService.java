package com.traction.service;

import com.traction.dto.request.VehicleRequest;
import com.traction.dto.response.VehicleResponse;
import com.traction.entity.Vehicle;
import com.traction.entity.VehicleStatus;
import com.traction.exception.DuplicateResourceException;
import com.traction.exception.ResourceNotFoundException;
import com.traction.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    // ─────────────────────────────────────────────────────────────────
    // Create
    // ─────────────────────────────────────────────────────────────────

    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        if (vehicleRepository.existsByVin(request.getVin())) {
            throw new DuplicateResourceException("VIN already exists: " + request.getVin());
        }

        Vehicle vehicle = Vehicle.builder()
                .make(request.getMake())
                .model(request.getModel())
                .year(request.getYear())
                .color(request.getColor())
                .vin(request.getVin())
                .price(request.getPrice())
                .mileage(request.getMileage())
                .description(request.getDescription())
                .status(VehicleStatus.AVAILABLE)
                .build();

        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    // ─────────────────────────────────────────────────────────────────
    // Read
    // ─────────────────────────────────────────────────────────────────

    public VehicleResponse findById(UUID id) {
        return VehicleResponse.from(getOrThrow(id));
    }

    public Page<VehicleResponse> search(
            String make,
            String model,
            Integer year,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            VehicleStatus status,
            Pageable pageable) {
        return vehicleRepository
                .search(make, model, year, minPrice, maxPrice,
                        status != null ? status.name() : null, pageable)
                .map(VehicleResponse::from);
    }

    // ─────────────────────────────────────────────────────────────────
    // Update
    // ─────────────────────────────────────────────────────────────────

    @Transactional
    public VehicleResponse update(UUID id, VehicleRequest request) {
        Vehicle vehicle = getOrThrow(id);

        // VIN collision guard: only reject if the VIN belongs to a *different* vehicle
        if (!vehicle.getVin().equals(request.getVin())) {
            vehicleRepository.findByVin(request.getVin()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DuplicateResourceException("VIN already in use: " + request.getVin());
                }
            });
        }

        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setColor(request.getColor());
        vehicle.setVin(request.getVin());
        vehicle.setPrice(request.getPrice());
        vehicle.setMileage(request.getMileage());
        vehicle.setDescription(request.getDescription());

        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Transactional
    public VehicleResponse updateStatus(UUID id, VehicleStatus status) {
        Vehicle vehicle = getOrThrow(id);
        vehicle.setStatus(status);
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    // ─────────────────────────────────────────────────────────────────
    // Delete
    // ─────────────────────────────────────────────────────────────────

    @Transactional
    public void delete(UUID id) {
        Vehicle vehicle = getOrThrow(id);
        vehicleRepository.delete(vehicle);
    }

    // ─────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────

    private Vehicle getOrThrow(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }
}
