package com.traction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Make is required")
    @Size(max = 50)
    @Column(nullable = false)
    private String make;

    @NotBlank(message = "Model is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String model;

    @Min(value = 1886, message = "Year must be 1886 or later")
    @Max(value = 2100, message = "Year must be 2100 or earlier")
    @Column(nullable = false)
    private int year;

    @NotBlank(message = "Color is required")
    @Size(max = 30)
    @Column(nullable = false)
    private String color;

    @NotBlank(message = "VIN is required")
    @Size(min = 5, max = 17, message = "VIN must be between 5 and 17 characters")
    @Column(unique = true, nullable = false, length = 17)
    private String vin;

    @Positive(message = "Price must be positive")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @PositiveOrZero(message = "Mileage cannot be negative")
    @Column(nullable = false)
    private int mileage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 512)
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
