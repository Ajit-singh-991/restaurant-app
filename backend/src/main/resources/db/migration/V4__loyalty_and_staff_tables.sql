-- Loyalty Program tables
CREATE TABLE loyalty_accounts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    points INTEGER NOT NULL DEFAULT 0,
    total_points_earned INTEGER NOT NULL DEFAULT 0,
    tier VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loyalty_transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES loyalty_accounts(id) ON DELETE CASCADE,
    order_id BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    type VARCHAR(20) NOT NULL,
    points INTEGER NOT NULL,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_loyalty_tx_account ON loyalty_transactions(account_id);
CREATE INDEX idx_loyalty_tx_order ON loyalty_transactions(order_id);

-- Staff time tracking table
CREATE TABLE time_entries (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    clock_in TIMESTAMP NOT NULL,
    clock_out TIMESTAMP,
    hours_worked NUMERIC(10,2),
    tips NUMERIC(10,2) DEFAULT 0,
    notes VARCHAR(200),
    CONSTRAINT chk_clock_out CHECK (clock_out IS NULL OR clock_out >= clock_in)
);

CREATE INDEX idx_time_entries_staff ON time_entries(staff_id);
CREATE INDEX idx_time_entries_clock_in ON time_entries(clock_in);
