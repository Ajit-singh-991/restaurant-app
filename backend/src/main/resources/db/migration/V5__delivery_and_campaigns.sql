-- Delivery tracking table
CREATE TABLE delivery_tracking (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    driver_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    delivery_address VARCHAR(300),
    city VARCHAR(100),
    postal_code VARCHAR(20),
    delivery_instructions VARCHAR(500),
    contact_phone VARCHAR(20),
    delivery_fee NUMERIC(10,2) DEFAULT 0,
    estimated_minutes INTEGER,
    assigned_at TIMESTAMP,
    picked_up_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_delivery_order ON delivery_tracking(order_id);
CREATE INDEX idx_delivery_driver ON delivery_tracking(driver_id);
CREATE INDEX idx_delivery_status ON delivery_tracking(status);

-- Marketing campaigns table
CREATE TABLE campaigns (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    discount_percent NUMERIC(5,2),
    discount_amount NUMERIC(10,2),
    promo_code VARCHAR(50) UNIQUE,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    target_segment VARCHAR(50),
    usage_count INTEGER NOT NULL DEFAULT 0,
    max_usage INTEGER,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_campaigns_status ON campaigns(status);
CREATE INDEX idx_campaigns_promo ON campaigns(promo_code);
