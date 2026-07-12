-- V3: Create purchases table
-- Links users and vehicles for purchase history tracking.

CREATE TABLE purchases (
    id           UUID            PRIMARY KEY,
    user_id      UUID            NOT NULL REFERENCES users(id),
    vehicle_id   UUID            NOT NULL REFERENCES vehicles(id),
    total_price  NUMERIC(12, 2)  NOT NULL,
    purchased_at TIMESTAMP       NOT NULL,

    CONSTRAINT chk_total_price CHECK (total_price >= 0)
);

CREATE INDEX idx_purchases_user_id    ON purchases (user_id);
CREATE INDEX idx_purchases_vehicle_id ON purchases (vehicle_id);
