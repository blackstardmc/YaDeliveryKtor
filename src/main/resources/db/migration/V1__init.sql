CREATE TABLE users (
                       id UUID PRIMARY KEY,

                       email VARCHAR(255) UNIQUE,

                       phone VARCHAR(20) NOT NULL UNIQUE,

                       password_hash VARCHAR(255) NOT NULL,

                       first_name VARCHAR(100) NOT NULL,

                       last_name VARCHAR(100) NOT NULL,

                       roles VARCHAR(255) NOT NULL,

                       is_active BOOLEAN NOT NULL DEFAULT TRUE,

                       created_at TIMESTAMP NOT NULL,

                       updated_at TIMESTAMP NOT NULL
);

CREATE TABLE addresses (

                           id UUID PRIMARY KEY,

                           user_id UUID NOT NULL,

                           label VARCHAR(100),

                           street VARCHAR(255) NOT NULL,

                           number VARCHAR(30),

                           neighborhood VARCHAR(100),

                           city VARCHAR(100) NOT NULL,

                           province VARCHAR(100),

                           reference TEXT,

                           latitude DOUBLE PRECISION,

                           longitude DOUBLE PRECISION,

                           is_default BOOLEAN NOT NULL DEFAULT FALSE,

                           created_at TIMESTAMP NOT NULL,

                           updated_at TIMESTAMP NOT NULL,

                           CONSTRAINT fk_address_user
                               FOREIGN KEY (user_id)
                                   REFERENCES users(id)
                                   ON DELETE CASCADE
);

CREATE TABLE restaurants (

                             id UUID PRIMARY KEY,

                             owner_id UUID NOT NULL,

                             address_id UUID NOT NULL,

                             name VARCHAR(200) NOT NULL,

                             description TEXT,

                             phone VARCHAR(20),

                             status VARCHAR(30) NOT NULL,

                             created_at TIMESTAMP NOT NULL,

                             updated_at TIMESTAMP NOT NULL,

                             CONSTRAINT fk_restaurant_owner
                                 FOREIGN KEY (owner_id)
                                     REFERENCES users(id),

                             CONSTRAINT fk_restaurant_address
                                 FOREIGN KEY (address_id)
                                     REFERENCES addresses(id)
);


CREATE TABLE categories (

                            id UUID PRIMARY KEY,

                            restaurant_id UUID NOT NULL,

                            name VARCHAR(120) NOT NULL,

                            description TEXT,

                            sort_order INTEGER NOT NULL,

                            is_active BOOLEAN NOT NULL DEFAULT TRUE,

                            created_at TIMESTAMP NOT NULL,

                            updated_at TIMESTAMP NOT NULL,

                            CONSTRAINT fk_category_restaurant
                                FOREIGN KEY (restaurant_id)
                                    REFERENCES restaurants(id)
                                    ON DELETE CASCADE
);

CREATE TABLE products (

                          id UUID PRIMARY KEY,

                          restaurant_id UUID NOT NULL,

                          category_id UUID,

                          name VARCHAR(200) NOT NULL,

                          description TEXT,

                          price BIGINT NOT NULL,

                          is_available BOOLEAN NOT NULL DEFAULT TRUE,

                          created_at TIMESTAMP NOT NULL,

                          updated_at TIMESTAMP NOT NULL,

                          CONSTRAINT fk_product_restaurant
                              FOREIGN KEY (restaurant_id)
                                  REFERENCES restaurants(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_product_category
                              FOREIGN KEY (category_id)
                                  REFERENCES categories(id)
                                  ON DELETE SET NULL
);

CREATE TABLE drivers (

                         id UUID PRIMARY KEY,

                         user_id UUID NOT NULL UNIQUE,

                         status VARCHAR(30) NOT NULL,

                         vehicle_type VARCHAR(30),

                         vehicle_description VARCHAR(255),

                         created_at TIMESTAMP NOT NULL,

                         updated_at TIMESTAMP NOT NULL,

                         CONSTRAINT fk_driver_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(id)
);

CREATE TABLE orders (

                        id UUID PRIMARY KEY,

                        customer_id UUID NOT NULL,

                        restaurant_id UUID NOT NULL,

                        driver_id UUID,

                        delivery_address_id UUID NOT NULL,

                        status VARCHAR(30) NOT NULL,

                        subtotal BIGINT NOT NULL,

                        delivery_fee BIGINT NOT NULL,

                        total BIGINT NOT NULL,

                        notes TEXT,

                        created_at TIMESTAMP NOT NULL,

                        confirmed_at TIMESTAMP,

                        preparing_at TIMESTAMP,

                        ready_at TIMESTAMP,

                        picked_up_at TIMESTAMP,

                        delivered_at TIMESTAMP,

                        cancelled_at TIMESTAMP,

                        updated_at TIMESTAMP NOT NULL,

                        CONSTRAINT fk_order_customer
                            FOREIGN KEY (customer_id)
                                REFERENCES users(id),

                        CONSTRAINT fk_order_restaurant
                            FOREIGN KEY (restaurant_id)
                                REFERENCES restaurants(id),

                        CONSTRAINT fk_order_driver
                            FOREIGN KEY (driver_id)
                                REFERENCES drivers(id),

                        CONSTRAINT fk_order_address
                            FOREIGN KEY (delivery_address_id)
                                REFERENCES addresses(id)
);


CREATE TABLE order_items (

                             id UUID PRIMARY KEY,

                             order_id UUID NOT NULL,

                             product_id UUID NOT NULL,

                             product_name VARCHAR(200) NOT NULL,

                             unit_price BIGINT NOT NULL,

                             quantity INTEGER NOT NULL,

                             subtotal BIGINT NOT NULL,

                             CONSTRAINT fk_item_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_item_product
                                 FOREIGN KEY (product_id)
                                     REFERENCES products(id)
);


CREATE TABLE order_status_history (

                                      id UUID PRIMARY KEY,

                                      order_id UUID NOT NULL,

                                      from_status VARCHAR(30),

                                      to_status VARCHAR(30) NOT NULL,

                                      changed_by UUID,

                                      created_at TIMESTAMP NOT NULL,

                                      CONSTRAINT fk_history_order
                                          FOREIGN KEY (order_id)
                                              REFERENCES orders(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_history_user
                                          FOREIGN KEY (changed_by)
                                              REFERENCES users(id)
);

CREATE INDEX idx_users_email
    ON users(email);

CREATE INDEX idx_users_phone
    ON users(phone);

CREATE INDEX idx_restaurant_owner
    ON restaurants(owner_id);

CREATE INDEX idx_category_restaurant
    ON categories(restaurant_id);

CREATE INDEX idx_product_restaurant
    ON products(restaurant_id);

CREATE INDEX idx_product_category
    ON products(category_id);

CREATE INDEX idx_driver_status
    ON drivers(status);

CREATE INDEX idx_order_customer
    ON orders(customer_id);

CREATE INDEX idx_order_restaurant
    ON orders(restaurant_id);

CREATE INDEX idx_order_driver
    ON orders(driver_id);

CREATE INDEX idx_order_status
    ON orders(status);

CREATE INDEX idx_item_order
    ON order_items(order_id);

CREATE INDEX idx_history_order
    ON order_status_history(order_id);




ALTER TABLE products
    ADD CONSTRAINT chk_product_price
        CHECK (price >= 0);

ALTER TABLE orders
    ADD CONSTRAINT chk_order_subtotal
        CHECK (subtotal >= 0);

ALTER TABLE orders
    ADD CONSTRAINT chk_delivery_fee
        CHECK (delivery_fee >= 0);

ALTER TABLE orders
    ADD CONSTRAINT chk_total
        CHECK (total >= 0);

ALTER TABLE order_items
    ADD CONSTRAINT chk_quantity
        CHECK (quantity > 0);

ALTER TABLE order_items
    ADD CONSTRAINT chk_item_price
        CHECK (unit_price >= 0);

ALTER TABLE order_items
    ADD CONSTRAINT chk_item_subtotal
        CHECK (subtotal >= 0);

CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role VARCHAR(30) NOT NULL,
                            PRIMARY KEY (user_id, role),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);