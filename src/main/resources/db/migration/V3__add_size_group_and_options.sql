ALTER TABLE products DROP COLUMN IF EXISTS size_group_id;
DROP TABLE IF EXISTS size_options;
DROP TABLE IF EXISTS size_groups;

CREATE TABLE size_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE size_options (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    size_group_id BIGINT NOT NULL REFERENCES size_groups(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_size_option_name ON size_options(name);
CREATE INDEX idx_size_option_group ON size_options(size_group_id);

ALTER TABLE products ADD COLUMN size_group_id BIGINT REFERENCES size_groups(id);
CREATE INDEX idx_product_size_group_id ON products(size_group_id);
