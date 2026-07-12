-- V2: Create vehicles table
-- Stores all vehicle inventory records for the dealership.
-- NOTE: status uses VARCHAR(20) + CHECK constraint so Hibernate's
-- @Enumerated(EnumType.STRING) binds cleanly without custom type casting.

CREATE TABLE vehicles (
    id          UUID            PRIMARY KEY,
    make        VARCHAR(50)     NOT NULL,
    model       VARCHAR(100)    NOT NULL,
    year        INTEGER         NOT NULL,
    color       VARCHAR(30)     NOT NULL,
    vin         VARCHAR(17)     UNIQUE NOT NULL,
    price       NUMERIC(12, 2)  NOT NULL,
    mileage     INTEGER         NOT NULL DEFAULT 0,
    status      VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',
    description TEXT,
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL,

    CONSTRAINT chk_year    CHECK (year    >= 1886 AND year    <= 2100),
    CONSTRAINT chk_price   CHECK (price   >  0),
    CONSTRAINT chk_mileage CHECK (mileage >= 0),
    CONSTRAINT chk_status  CHECK (status IN ('AVAILABLE', 'RESERVED', 'SOLD'))
);

CREATE INDEX idx_vehicles_make_model ON vehicles (make, model);
CREATE INDEX idx_vehicles_status     ON vehicles (status);
CREATE INDEX idx_vehicles_year       ON vehicles (year);
CREATE INDEX idx_vehicles_price      ON vehicles (price);
