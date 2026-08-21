CREATE TABLE users
(
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) UNIQUE,
    phone         VARCHAR(20)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL
);


CREATE TABLE user_roles
(
    user_id UUID        NOT NULL,
    role    VARCHAR(30) NOT NULL,

    PRIMARY KEY (user_id, role),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_user_roles_role
        CHECK (
            role IN (
                     'CUSTOMER',
                     'RESTAURANT',
                     'DRIVER',
                     'ADMIN'
                )
            )
);


CREATE TABLE addresses
(
    id           UUID PRIMARY KEY,
    user_id      UUID         NOT NULL,
    label        VARCHAR(100),
    street       VARCHAR(255) NOT NULL,
    number       VARCHAR(30),
    neighborhood VARCHAR(100),
    city         VARCHAR(100) NOT NULL,
    province     VARCHAR(100),
    reference    TEXT,
    latitude     DOUBLE PRECISION,
    longitude    DOUBLE PRECISION,
    is_default   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,

    CONSTRAINT fk_addresses_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_addresses_latitude
        CHECK (
            latitude IS NULL
                OR latitude BETWEEN -90 AND 90
            ),

    CONSTRAINT chk_addresses_longitude
        CHECK (
            longitude IS NULL
                OR longitude BETWEEN -180 AND 180
            )
);


CREATE TABLE restaurants
(
    id          UUID PRIMARY KEY,
    owner_id    UUID         NOT NULL,
    address_id  UUID         NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    phone       VARCHAR(20),
    status      VARCHAR(30)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT fk_restaurants_owner
        FOREIGN KEY (owner_id)
            REFERENCES users (id),

    CONSTRAINT fk_restaurants_address
        FOREIGN KEY (address_id)
            REFERENCES addresses (id),

    CONSTRAINT chk_restaurants_status
        CHECK (
            status IN (
                       'ACTIVE',
                       'INACTIVE',
                       'SUSPENDED'
                )
            )
);


CREATE TABLE categories
(
    id            UUID PRIMARY KEY,
    restaurant_id UUID         NOT NULL,
    name          VARCHAR(120) NOT NULL,
    description   TEXT,
    sort_order    INTEGER      NOT NULL DEFAULT 0,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,

    CONSTRAINT fk_categories_restaurant
        FOREIGN KEY (restaurant_id)
            REFERENCES restaurants (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_categories_sort_order
        CHECK (sort_order >= 0)
);


CREATE TABLE products
(
    id            UUID PRIMARY KEY,
    restaurant_id UUID         NOT NULL,
    category_id   UUID,
    name          VARCHAR(200) NOT NULL,
    description   TEXT,
    price         BIGINT       NOT NULL,
    is_available  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,

    CONSTRAINT fk_products_restaurant
        FOREIGN KEY (restaurant_id)
            REFERENCES restaurants (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
            REFERENCES categories (id)
            ON DELETE SET NULL,

    CONSTRAINT chk_products_price
        CHECK (price >= 0)
);


CREATE TABLE drivers
(
    id                  UUID PRIMARY KEY,
    user_id             UUID        NOT NULL UNIQUE,
    status              VARCHAR(30) NOT NULL,
    vehicle_type        VARCHAR(30),
    vehicle_description VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_drivers_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);


CREATE TABLE orders
(
    id                  UUID PRIMARY KEY,
    customer_id         UUID        NOT NULL,
    restaurant_id       UUID        NOT NULL,
    driver_id           UUID,
    delivery_address_id UUID        NOT NULL,
    status              VARCHAR(30) NOT NULL,
    subtotal            BIGINT      NOT NULL,
    delivery_fee        BIGINT      NOT NULL,
    total               BIGINT      NOT NULL,
    notes               TEXT,
    created_at          TIMESTAMPTZ NOT NULL,
    confirmed_at        TIMESTAMPTZ,
    preparing_at        TIMESTAMPTZ,
    ready_at            TIMESTAMPTZ,
    picked_up_at        TIMESTAMPTZ,
    delivered_at        TIMESTAMPTZ,
    cancelled_at        TIMESTAMPTZ,
    updated_at          TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id)
            REFERENCES users (id),

    CONSTRAINT fk_orders_restaurant
        FOREIGN KEY (restaurant_id)
            REFERENCES restaurants (id),

    CONSTRAINT fk_orders_driver
        FOREIGN KEY (driver_id)
            REFERENCES drivers (id),

    CONSTRAINT fk_orders_address
        FOREIGN KEY (delivery_address_id)
            REFERENCES addresses (id),

    CONSTRAINT chk_orders_money
        CHECK (
            subtotal >= 0
                AND delivery_fee >= 0
                AND total >= 0
            ),

    CONSTRAINT chk_orders_total
        CHECK (
            total = subtotal + delivery_fee
            )
);


CREATE TABLE order_items
(
    id           UUID PRIMARY KEY,
    order_id     UUID         NOT NULL,
    product_id   UUID         NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    unit_price   BIGINT       NOT NULL,
    quantity     INTEGER      NOT NULL,
    subtotal     BIGINT       NOT NULL,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
            REFERENCES orders (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id)
            REFERENCES products (id),

    CONSTRAINT chk_order_items_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_order_items_price
        CHECK (unit_price >= 0),

    CONSTRAINT chk_order_items_subtotal
        CHECK (
            subtotal = unit_price * quantity
            )
);


CREATE TABLE order_status_history
(
    id          UUID PRIMARY KEY,
    order_id    UUID        NOT NULL,
    from_status VARCHAR(30),
    to_status   VARCHAR(30) NOT NULL,
    changed_by  UUID,
    created_at  TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_order_history_order
        FOREIGN KEY (order_id)
            REFERENCES orders (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_order_history_user
        FOREIGN KEY (changed_by)
            REFERENCES users (id)
            ON DELETE SET NULL
);


CREATE INDEX idx_addresses_user
    ON addresses (user_id);

CREATE INDEX idx_restaurants_owner
    ON restaurants (owner_id);

CREATE INDEX idx_categories_restaurant
    ON categories (restaurant_id);

CREATE INDEX idx_products_restaurant
    ON products (restaurant_id);

CREATE INDEX idx_products_category
    ON products (category_id);

CREATE INDEX idx_products_restaurant_available
    ON products (restaurant_id, is_available);

CREATE INDEX idx_drivers_status
    ON drivers (status);

CREATE INDEX idx_orders_customer
    ON orders (customer_id);

CREATE INDEX idx_orders_restaurant
    ON orders (restaurant_id);

CREATE INDEX idx_orders_driver
    ON orders (driver_id);

CREATE INDEX idx_orders_status
    ON orders (status);

CREATE INDEX idx_orders_created_at
    ON orders (created_at);

CREATE INDEX idx_order_items_order
    ON order_items (order_id);

CREATE INDEX idx_order_history_order
    ON order_status_history (order_id);