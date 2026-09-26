CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY,

                                user_id UUID NOT NULL,

                                token_hash VARCHAR(64) NOT NULL UNIQUE,

                                expires_at TIMESTAMPTZ NOT NULL,

                                revoked_at TIMESTAMPTZ,

                                created_at TIMESTAMPTZ NOT NULL,

                                CONSTRAINT fk_refresh_tokens_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE
);


CREATE INDEX idx_refresh_tokens_user
    ON refresh_tokens(user_id);


CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens(expires_at);


CREATE INDEX idx_refresh_tokens_active_user
    ON refresh_tokens(user_id)
    WHERE revoked_at IS NULL;