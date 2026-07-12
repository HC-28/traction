package com.traction.controller;

import com.traction.dto.request.VehicleRequest;
import com.traction.dto.response.ApiResponse;
import com.traction.dto.response.VehicleResponse;
import com.traction.entity.VehicleStatus;
import com.traction.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle Inventory Management Endpoints")
@SecurityRequirement(name = "bearerAuth")
public class VehicleController {

    private final VehicleService vehicleService;

    // ─────────────────────────────────────────────────────────────────
    // POST /api/vehicles — ADMIN only
    // ─────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new vehicle listing (ADMIN)")
    public ResponseEntity<ApiResponse<VehicleResponse>> create(
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle created successfully", response));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/vehicles/{id} — any authenticated user
    // ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID")
    public ResponseEntity<ApiResponse<VehicleResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.findById(id)));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/vehicles — paginated search with optional filters
    // ─────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Search / list vehicles with optional filters")
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> search(
            @RequestParam(required = false) String make,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) VehicleStatus status,
            @PageableDefault(size = 20, sort = "created_at") Pageable pageable) {
        Page<VehicleResponse> page = vehicleService.search(make, model, year, minPrice, maxPrice, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    // ─────────────────────────────────────────────────────────────────
    // PUT /api/vehicles/{id} — ADMIN only
    // ─────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Full update of a vehicle listing (ADMIN)")
    public ResponseEntity<ApiResponse<VehicleResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", vehicleService.update(id, request)));
    }

    // ─────────────────────────────────────────────────────────────────
    // PATCH /api/vehicles/{id}/status — ADMIN only
    // ─────────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update vehicle status only (ADMIN)")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestParam VehicleStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", vehicleService.updateStatus(id, status)));
    }

    // ─────────────────────────────────────────────────────────────────
    // DELETE /api/vehicles/{id} — ADMIN only
    // ─────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a vehicle listing (ADMIN)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /api/vehicles/{id}/image — ADMIN only (Cloudinary upload)
    // ─────────────────────────────────────────────────────────────────

    @PostMapping(value = "/{id}/image", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upload vehicle photo to Cloudinary (ADMIN)")
    public ResponseEntity<ApiResponse<VehicleResponse>> uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file)
            throws java.io.IOException {
        VehicleResponse response = vehicleService.uploadImage(id, file);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", response));
    }
}
