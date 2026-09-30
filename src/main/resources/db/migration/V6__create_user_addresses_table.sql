CREATE TABLE user_addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    province VARCHAR(255) NOT NULL,
    district VARCHAR(255) NOT NULL,
    ward VARCHAR(255) NOT NULL,
    detail_address TEXT NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'HOME',
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_addresses_user
        FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT ck_user_addresses_type
        CHECK (type IN ('HOME', 'OFFICE', 'OTHER'))
);

CREATE UNIQUE INDEX uq_user_addresses_default
    ON user_addresses (user_id) WHERE is_default = true;

CREATE INDEX idx_user_addresses_user ON user_addresses (user_id);
