CREATE UNIQUE INDEX uq_addresses_default_per_user
    ON addresses(user_id)
    WHERE is_default = TRUE;