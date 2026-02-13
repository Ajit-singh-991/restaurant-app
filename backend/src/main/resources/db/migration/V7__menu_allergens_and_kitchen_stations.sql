-- Menu: add allergens (comma-separated or JSON; using VARCHAR for simplicity)
ALTER TABLE menu_items ADD COLUMN IF NOT EXISTS allergens VARCHAR(500);

-- Kitchen stations
CREATE TABLE kitchen_stations (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    display_order   INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_kitchen_stations_display_order ON kitchen_stations(display_order);

-- Link menu items to default preparation station (optional)
ALTER TABLE menu_items ADD COLUMN IF NOT EXISTS station_id BIGINT REFERENCES kitchen_stations(id);
CREATE INDEX idx_menu_items_station ON menu_items(station_id);

-- Link order items to station (set when order is created from menu_item.station_id)
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS station_id BIGINT REFERENCES kitchen_stations(id);
CREATE INDEX idx_order_items_station ON order_items(station_id);

-- Seed default stations
INSERT INTO kitchen_stations (name, display_order) VALUES
    ('Grill', 1),
    ('Cold / Salad', 2),
    ('Dessert', 3);
