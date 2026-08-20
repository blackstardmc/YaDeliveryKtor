ALTER TABLE orders
    ADD CONSTRAINT chk_orders_status
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
            );

ALTER TABLE order_status_history
    ADD CONSTRAINT chk_order_history_to_status
        CHECK (
            to_status IN (
                          'RECEIVED',
                          'CONFIRMED',
                          'PREPARING',
                          'READY',
                          'IN_DELIVERY',
                          'DELIVERED',
                          'CANCELLED'
                )
            );

ALTER TABLE order_status_history
    ADD CONSTRAINT chk_order_history_from_status
        CHECK (
            from_status IS NULL
                OR from_status IN (
                                   'RECEIVED',
                                   'CONFIRMED',
                                   'PREPARING',
                                   'READY',
                                   'IN_DELIVERY',
                                   'DELIVERED',
                                   'CANCELLED'
                )
            );

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_total_calculation
        CHECK (
            total = subtotal + delivery_fee
            );

ALTER TABLE order_items
    ADD CONSTRAINT chk_order_items_subtotal_calculation
        CHECK (
            subtotal = unit_price * quantity
            );