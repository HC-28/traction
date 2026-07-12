package com.traction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {
    private UUID purchaseId;
    private UUID vehicleId;
    private String vehicleName;
    private BigDecimal totalPrice;
    private LocalDateTime purchasedAt;
}
