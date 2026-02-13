-- Invoices: one per order, generated when payment is processed
CREATE TABLE invoices (
    id               BIGSERIAL PRIMARY KEY,
    order_id         BIGINT        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    invoice_number   VARCHAR(30)   NOT NULL UNIQUE,
    bill_to_name     VARCHAR(200),
    bill_to_contact  VARCHAR(100),
    items_json       TEXT,
    subtotal         DECIMAL(10,2) NOT NULL,
    tax_amount       DECIMAL(10,2) NOT NULL,
    total_amount     DECIMAL(10,2) NOT NULL,
    generated_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    generated_by_id  BIGINT        REFERENCES users(id)
);

CREATE INDEX idx_invoices_order ON invoices(order_id);
CREATE INDEX idx_invoices_number ON invoices(invoice_number);
