ALTER TABLE drivers
    ADD CONSTRAINT chk_drivers_status
        CHECK (
            status IN (
                       'OFFLINE',
                       'AVAILABLE',
                       'BUSY',
                       'SUSPENDED'
                )
            );

ALTER TABLE drivers
    ADD CONSTRAINT chk_drivers_vehicle_type
        CHECK (
            vehicle_type IS NULL
                OR vehicle_type IN (
                                    'BICYCLE',
                                    'MOTORCYCLE',
                                    'CAR',
                                    'OTHER'
                )
            );