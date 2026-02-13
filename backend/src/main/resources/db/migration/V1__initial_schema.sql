-- Restaurant Management System - Initial Schema
-- PostgreSQL

-- =============================================
-- USERS & AUTHENTICATION
-- =============================================

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100) UNIQUE,
    phone           VARCHAR(20),
    role            VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER'
                    CHECK (role IN ('ADMIN', 'MANAGER', 'WAITER', 'KITCHEN', 'CUSTOMER')),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_email ON users(email);

-- =============================================
-- MENU
-- =============================================

CREATE TABLE categories (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50)  NOT NULL UNIQUE,
    description     VARCHAR(200),
    display_order   INTEGER,
    active          BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE menu_items (
    id                       BIGSERIAL PRIMARY KEY,
    name                     VARCHAR(100)   NOT NULL,
    description              VARCHAR(500),
    price                    DECIMAL(10,2)  NOT NULL CHECK (price > 0),
    category_id              BIGINT         NOT NULL REFERENCES categories(id),
    image_url                VARCHAR(500),
    available                BOOLEAN        NOT NULL DEFAULT TRUE,
    preparation_time_minutes INTEGER,
    vegetarian               BOOLEAN        NOT NULL DEFAULT FALSE,
    vegan                    BOOLEAN        NOT NULL DEFAULT FALSE,
    gluten_free              BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at               TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_menu_items_category ON menu_items(category_id);
CREATE INDEX idx_menu_items_available ON menu_items(available);

-- =============================================
-- TABLE MANAGEMENT
-- =============================================

CREATE TABLE restaurant_tables (
    id              BIGSERIAL PRIMARY KEY,
    table_number    INTEGER     NOT NULL UNIQUE,
    capacity        INTEGER     NOT NULL CHECK (capacity > 0),
    status          VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
                    CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE')),
    section         VARCHAR(50)
);

CREATE INDEX idx_tables_status ON restaurant_tables(status);

-- =============================================
-- RESERVATIONS
-- =============================================

CREATE TABLE reservations (
    id                BIGSERIAL PRIMARY KEY,
    customer_id       BIGINT      REFERENCES users(id),
    table_id          BIGINT      REFERENCES restaurant_tables(id),
    customer_name     VARCHAR(100) NOT NULL,
    customer_phone    VARCHAR(20),
    customer_email    VARCHAR(100),
    reservation_date  DATE         NOT NULL,
    reservation_time  TIME         NOT NULL,
    party_size        INTEGER      NOT NULL CHECK (party_size > 0),
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                      CHECK (status IN ('PENDING', 'CONFIRMED', 'SEATED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    special_requests  VARCHAR(500),
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reservations_date ON reservations(reservation_date);
CREATE INDEX idx_reservations_customer ON reservations(customer_id);

-- =============================================
-- ORDERS
-- =============================================

CREATE TABLE orders (
    id                   BIGSERIAL PRIMARY KEY,
    order_number         VARCHAR(20)   NOT NULL UNIQUE,
    table_id             BIGINT        REFERENCES restaurant_tables(id),
    waiter_id            BIGINT        REFERENCES users(id),
    customer_id          BIGINT        REFERENCES users(id),
    status               VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                         CHECK (status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'SERVED', 'COMPLETED', 'CANCELLED')),
    order_type           VARCHAR(20)   NOT NULL DEFAULT 'DINE_IN'
                         CHECK (order_type IN ('DINE_IN', 'TAKEAWAY', 'DELIVERY')),
    subtotal             DECIMAL(10,2),
    tax_amount           DECIMAL(10,2),
    total_amount         DECIMAL(10,2),
    special_instructions VARCHAR(500),
    created_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at         TIMESTAMP
);

CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_table ON orders(table_id);
CREATE INDEX idx_orders_waiter ON orders(waiter_id);
CREATE INDEX idx_orders_created ON orders(created_at);
CREATE INDEX idx_orders_number ON orders(order_number);

CREATE TABLE order_items (
    id               BIGSERIAL PRIMARY KEY,
    order_id         BIGINT        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id     BIGINT        NOT NULL REFERENCES menu_items(id),
    quantity         INTEGER       NOT NULL CHECK (quantity > 0),
    unit_price       DECIMAL(10,2) NOT NULL,
    total_price      DECIMAL(10,2),
    special_requests VARCHAR(300),
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING', 'PREPARING', 'READY', 'SERVED', 'CANCELLED'))
);

CREATE INDEX idx_order_items_order ON order_items(order_id);

-- =============================================
-- ORDER STATUS HISTORY
-- =============================================

CREATE TABLE order_status_history (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT      NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    status      VARCHAR(20) NOT NULL,
    changed_by  BIGINT      REFERENCES users(id),
    notes       VARCHAR(500),
    changed_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_history_order ON order_status_history(order_id);

-- =============================================
-- PAYMENTS
-- =============================================

CREATE TABLE payments (
    id               BIGSERIAL PRIMARY KEY,
    order_id         BIGINT        NOT NULL REFERENCES orders(id),
    amount           DECIMAL(10,2) NOT NULL,
    payment_method   VARCHAR(20)   NOT NULL
                     CHECK (payment_method IN ('CASH', 'CARD', 'UPI', 'WALLET')),
    transaction_id   VARCHAR(100),
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    refunded_at      TIMESTAMP,
    refund_reason    VARCHAR(300)
);

CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);

-- =============================================
-- INVENTORY
-- =============================================

CREATE TABLE ingredients (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(100)   NOT NULL,
    unit           VARCHAR(20)    NOT NULL,
    current_stock  DECIMAL(10,2)  NOT NULL DEFAULT 0,
    reorder_level  DECIMAL(10,2)  NOT NULL DEFAULT 0
);

CREATE TABLE suppliers (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    contact_person  VARCHAR(100),
    phone           VARCHAR(20),
    email           VARCHAR(100)
);

-- =============================================
-- CUSTOMERS
-- =============================================

CREATE TABLE customer_addresses (
    id            BIGSERIAL PRIMARY KEY,
    customer_id   BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    address_line1 VARCHAR(200) NOT NULL,
    address_line2 VARCHAR(200),
    city          VARCHAR(100) NOT NULL,
    postal_code   VARCHAR(20)  NOT NULL,
    is_default    BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_customer_addresses ON customer_addresses(customer_id);
