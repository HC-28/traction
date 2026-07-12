package com.traction.dto.response;

import com.traction.entity.Vehicle;
import com.traction.entity.VehicleStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {

    private UUID id;
    private String make;
    private String model;
    private int year;
    private String color;
    private String vin;
    private BigDecimal price;
    private int mileage;
    private VehicleStatus status;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VehicleResponse from(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .color(vehicle.getColor())
                .vin(vehicle.getVin())
                .price(vehicle.getPrice())
                .mileage(vehicle.getMileage())
                .status(vehicle.getStatus())
                .description(vehicle.getDescription())
                .imageUrl(vehicle.getImageUrl())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}
