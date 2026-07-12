package com.traction.repository;

import com.traction.entity.Vehicle;
import com.traction.entity.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    boolean existsByVin(String vin);

    Optional<Vehicle> findByVin(String vin);

    @Query(value = """
            SELECT * FROM vehicles v
            WHERE  (:make     IS NULL OR LOWER(v.make)  LIKE LOWER(CONCAT('%', :make,  '%')))
              AND  (:model    IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%')))
              AND  (:year     IS NULL OR v.year         = CAST(:year AS INTEGER))
              AND  (:minPrice IS NULL OR v.price        >= CAST(:minPrice AS NUMERIC))
              AND  (:maxPrice IS NULL OR v.price        <= CAST(:maxPrice AS NUMERIC))
              AND  (:status   IS NULL OR v.status       = :status)
            """,
            countQuery = """
            SELECT COUNT(*) FROM vehicles v
            WHERE  (:make     IS NULL OR LOWER(v.make)  LIKE LOWER(CONCAT('%', :make,  '%')))
              AND  (:model    IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%')))
              AND  (:year     IS NULL OR v.year         = CAST(:year AS INTEGER))
              AND  (:minPrice IS NULL OR v.price        >= CAST(:minPrice AS NUMERIC))
              AND  (:maxPrice IS NULL OR v.price        <= CAST(:maxPrice AS NUMERIC))
              AND  (:status   IS NULL OR v.status       = :status)
            """,
            nativeQuery = true)
    Page<Vehicle> search(
            @Param("make")     String make,
            @Param("model")    String model,
            @Param("year")     Integer year,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("status")   String status,
            Pageable pageable
    );
}
