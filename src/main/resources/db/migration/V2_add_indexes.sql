CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       email VARCHAR(255),

                       phone VARCHAR(32) NOT NULL,

                       password_hash VARCHAR(255) NOT NULL,

                       first_name VARCHAR(100) NOT NULL,

                       last_name VARCHAR(100) NOT NULL,

                       is_active BOOLEAN NOT NULL DEFAULT TRUE,

                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                       CONSTRAINT users_email_unique
                           UNIQUE (email),

                       CONSTRAINT users_phone_unique
                           UNIQUE (phone)
);

CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
                            user_id UUID NOT NULL,

                            role_id UUID NOT NULL,

                            PRIMARY KEY (user_id, role_id),

                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id)
                                    REFERENCES roles(id)
                                    ON DELETE CASCADE
);

CREATE TABLE addresses (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                           user_id UUID NOT NULL,

                           label VARCHAR(100),

                           street VARCHAR(255) NOT NULL,

                           number VARCHAR(50),

                           neighborhood VARCHAR(150),

                           city VARCHAR(150) NOT NULL,

                           province VARCHAR(150),

                           reference TEXT,

                           latitude DOUBLE PRECISION,

                           longitude DOUBLE PRECISION,

                           is_default BOOLEAN NOT NULL DEFAULT FALSE,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                           updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                           CONSTRAINT fk_addresses_user
                               FOREIGN KEY (user_id)
                                   REFERENCES users(id)
                                   ON DELETE CASCADE
);


CREATE TABLE restaurants (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                             owner_id UUID NOT NULL,

                             name VARCHAR(200) NOT NULL,

                             description TEXT,

                             phone VARCHAR(32),

                             address_id UUID NOT NULL,

                             status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                             updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                             CONSTRAINT fk_restaurants_owner
                                 FOREIGN KEY (owner_id)
                                     REFERENCES users(id),

                             CONSTRAINT fk_restaurants_address
                                 FOREIGN KEY (address_id)
                                     REFERENCES addresses(id),

                             CONSTRAINT restaurants_status_check
                                 CHECK (
                                     status IN (
                                                'ACTIVE',
                                                'INACTIVE',
                                                'SUSPENDED'
                                         )
                                     )
);


CREATE TABLE categories (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                            restaurant_id UUID NOT NULL,

                            name VARCHAR(150) NOT NULL,

                            description TEXT,

                            sort_order INTEGER NOT NULL DEFAULT 0,

                            is_active BOOLEAN NOT NULL DEFAULT TRUE,

                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                            updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                            CONSTRAINT fk_categories_restaurant
                                FOREIGN KEY (restaurant_id)
                                    REFERENCES restaurants(id)
                                    ON DELETE CASCADE
);


CREATE TABLE products (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          restaurant_id UUID NOT NULL,

                          category_id UUID,

                          name VARCHAR(200) NOT NULL,

                          description TEXT,

                          price_cents BIGINT NOT NULL,

                          is_available BOOLEAN NOT NULL DEFAULT TRUE,

                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                          CONSTRAINT products_price_check
                              CHECK (price_cents >= 0),

                          CONSTRAINT fk_products_restaurant
                              FOREIGN KEY (restaurant_id)
                                  REFERENCES restaurants(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_products_category
                              FOREIGN KEY (category_id)
                                  REFERENCES categories(id)
                                  ON DELETE SET NULL
);

CREATE TABLE drivers (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                         user_id UUID NOT NULL UNIQUE,

                         status VARCHAR(30) NOT NULL DEFAULT 'OFFLINE',

                         vehicle_type VARCHAR(30),

                         vehicle_description VARCHAR(255),

                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                         updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                         CONSTRAINT fk_drivers_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(id)
                                 ON DELETE CASCADE,

                         CONSTRAINT drivers_status_check
                             CHECK (
                                 status IN (
                                            'OFFLINE',
                                            'AVAILABLE',
                                            'BUSY',
                                            'SUSPENDED'
                                     )
                                 )
);


CREATE TABLE orders (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                        customer_id UUID NOT NULL,

                        restaurant_id UUID NOT NULL,

                        driver_id UUID,

                        delivery_address_id UUID NOT NULL,

                        status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',

                        subtotal_cents BIGINT NOT NULL,

                        delivery_fee_cents BIGINT NOT NULL,

                        total_cents BIGINT NOT NULL,

                        notes TEXT,

                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                        confirmed_at TIMESTAMPTZ,

                        preparing_at TIMESTAMPTZ,

                        ready_at TIMESTAMPTZ,

                        picked_up_at TIMESTAMPTZ,

                        delivered_at TIMESTAMPTZ,

                        cancelled_at TIMESTAMPTZ,

                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                        CONSTRAINT fk_orders_customer
                            FOREIGN KEY (customer_id)
                                REFERENCES users(id),

                        CONSTRAINT fk_orders_restaurant
                            FOREIGN KEY (restaurant_id)
                                REFERENCES restaurants(id),

                        CONSTRAINT fk_orders_driver
                            FOREIGN KEY (driver_id)
                                REFERENCES drivers(id),

                        CONSTRAINT fk_orders_delivery_address
                            FOREIGN KEY (delivery_address_id)
                                REFERENCES addresses(id),

                        CONSTRAINT orders_status_check
                            CHECK (
                                status IN (
                                           'RECEIVED',
                                           'CONFIRMED',
                                           'PREPARING',
                                           'READY',
                                           'IN_DELIVERY',
                                           'DELIVERED',
                                           'CANCELLED'
                                    )
                                ),

                        CONSTRAINT orders_subtotal_check
                            CHECK (subtotal_cents >= 0),

                        CONSTRAINT orders_delivery_fee_check
                            CHECK (delivery_fee_cents >= 0),

                        CONSTRAINT orders_total_check
                            CHECK (total_cents >= 0)
);


CREATE TABLE order_items (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                             order_id UUID NOT NULL,

                             product_id UUID NOT NULL,

                             product_name VARCHAR(200) NOT NULL,

                             unit_price_cents BIGINT NOT NULL,

                             quantity INTEGER NOT NULL,

                             subtotal_cents BIGINT NOT NULL,

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_order_items_product
                                 FOREIGN KEY (product_id)
                                     REFERENCES products(id),

                             CONSTRAINT order_items_quantity_check
                                 CHECK (quantity > 0),

                             CONSTRAINT order_items_price_check
                                 CHECK (unit_price_cents >= 0),

                             CONSTRAINT order_items_subtotal_check
                                 CHECK (subtotal_cents >= 0)
);


CREATE TABLE order_status_history (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                      order_id UUID NOT NULL,

                                      from_status VARCHAR(30),

                                      to_status VARCHAR(30) NOT NULL,

                                      changed_by UUID,

                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                      CONSTRAINT fk_order_history_order
                                          FOREIGN KEY (order_id)
                                              REFERENCES orders(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_order_history_user
                                          FOREIGN KEY (changed_by)
                                              REFERENCES users(id)
                                              ON DELETE SET NULL
);

CREATE INDEX idx_users_phone
    ON users(phone);

CREATE INDEX idx_users_email
    ON users(email);

CREATE INDEX idx_addresses_user_id
    ON addresses(user_id);

CREATE INDEX idx_restaurants_owner_id
    ON restaurants(owner_id);

CREATE INDEX idx_restaurants_status
    ON restaurants(status);

CREATE INDEX idx_categories_restaurant_id
    ON categories(restaurant_id);

CREATE INDEX idx_products_restaurant_id
    ON products(restaurant_id);

CREATE INDEX idx_products_category_id
    ON products(category_id);

CREATE INDEX idx_products_available
    ON products(restaurant_id, is_available);

CREATE INDEX idx_orders_customer_id
    ON orders(customer_id);

CREATE INDEX idx_orders_restaurant_id
    ON orders(restaurant_id);

CREATE INDEX idx_orders_driver_id
    ON orders(driver_id);

CREATE INDEX idx_orders_status
    ON orders(status);

CREATE INDEX idx_orders_created_at
    ON orders(created_at);

CREATE INDEX idx_order_items_order_id
    ON order_items(order_id);

CREATE INDEX idx_order_history_order_id
    ON order_status_history(order_id);