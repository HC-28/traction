package com.traction.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {

    @NotBlank(message = "Make is required")
    @Size(max = 50, message = "Make must not exceed 50 characters")
    private String make;

    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    @Min(value = 1886, message = "Year must be 1886 or later")
    @Max(value = 2100, message = "Year must be 2100 or earlier")
    private int year;

    @NotBlank(message = "Color is required")
    @Size(max = 30, message = "Color must not exceed 30 characters")
    private String color;

    @NotBlank(message = "VIN is required")
    @Size(min = 5, max = 17, message = "VIN must be between 5 and 17 characters")
    private String vin;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Digits(integer = 10, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;

    @PositiveOrZero(message = "Mileage cannot be negative")
    private int mileage;

    private String description;
}
