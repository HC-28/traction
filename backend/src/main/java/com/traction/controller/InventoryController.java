package com.traction.controller;

import com.traction.dto.response.ApiResponse;
import com.traction.dto.response.PurchaseResponse;
import com.traction.dto.response.VehicleResponse;
import com.traction.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory Purchase and Restock Management Endpoints")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/{id}/purchase")
    @Operation(summary = "Purchase a vehicle", description = "Allows a user to purchase a vehicle. Changes status to SOLD and logs purchase record.")
    public ResponseEntity<ApiResponse<PurchaseResponse>> purchase(@PathVariable UUID id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        PurchaseResponse response = inventoryService.purchase(id, username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/restock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Restock a vehicle", description = "Allows an admin to restock a SOLD vehicle back to AVAILABLE status.")
    public ResponseEntity<ApiResponse<VehicleResponse>> restock(@PathVariable UUID id) {
        VehicleResponse response = inventoryService.restock(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
