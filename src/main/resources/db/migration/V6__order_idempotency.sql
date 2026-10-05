CREATE TABLE order_requests (
    user_id UUID NOT NULL REFERENCES users(id),
    request_key VARCHAR(100) NOT NULL,
    fingerprint VARCHAR(64) NOT NULL,
    order_id UUID NOT NULL REFERENCES orders(id),
    PRIMARY KEY (user_id, request_key)
);

CREATE UNIQUE INDEX uq_driver_active_order ON orders(driver_id)
    WHERE driver_id IS NOT NULL AND status IN ('READY', 'IN_DELIVERY');
