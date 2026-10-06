CREATE TABLE membership_tiers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    display_names JSONB NOT NULL DEFAULT '{}'::jsonb,
    descriptions JSONB NOT NULL DEFAULT '{}'::jsonb,
    threshold NUMERIC(15, 2) NOT NULL,
    sort_order INTEGER NOT NULL,
    discount_percentage NUMERIC(5, 2) NOT NULL DEFAULT 0,
    free_shipping BOOLEAN NOT NULL DEFAULT FALSE,
    benefits JSONB NOT NULL DEFAULT '{}'::jsonb,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_membership_tier_threshold_non_negative CHECK (threshold >= 0),
    CONSTRAINT chk_membership_tier_discount_percentage CHECK (
        discount_percentage >= 0 AND discount_percentage <= 100
    )
);

CREATE INDEX idx_membership_tiers_active_order
    ON membership_tiers (active, sort_order);

CREATE INDEX idx_membership_tiers_active_threshold
    ON membership_tiers (active, threshold);

INSERT INTO membership_tiers (
    code,
    display_names,
    descriptions,
    threshold,
    sort_order,
    discount_percentage,
    free_shipping,
    benefits,
    active
)
SELECT
    tier::text,
    jsonb_build_object('vi', initcap(lower(tier::text)), 'en', initcap(lower(tier::text))),
    jsonb_build_object('vi', COALESCE(description, ''), 'en', COALESCE(description, '')),
    threshold,
    CASE tier::text
        WHEN 'BRONZE' THEN 1
        WHEN 'SILVER' THEN 2
        WHEN 'GOLD' THEN 3
        WHEN 'PLATINUM' THEN 4
        ELSE 1000
    END,
    0,
    FALSE,
    '{}'::jsonb,
    TRUE
FROM membership_tier_configs
ON CONFLICT (code) DO NOTHING;

DO $$
DECLARE
    active_tier_count INTEGER;
    base_tier_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO active_tier_count
    FROM membership_tiers
    WHERE active;

    SELECT COUNT(*) INTO base_tier_count
    FROM membership_tiers
    WHERE active AND threshold = 0;

    IF active_tier_count = 0 OR base_tier_count <> 1 THEN
        RAISE EXCEPTION 'V12 membership tier migration failed: active tiers must contain exactly one base tier at threshold zero';
    END IF;

    IF EXISTS (
        WITH ordered AS (
            SELECT
                threshold,
                sort_order,
                LAG(threshold) OVER (ORDER BY threshold, sort_order, id) AS previous_threshold,
                LAG(sort_order) OVER (ORDER BY threshold, sort_order, id) AS previous_sort_order
            FROM membership_tiers
            WHERE active
        )
        SELECT 1
        FROM ordered
        WHERE previous_threshold IS NOT NULL
          AND (threshold <= previous_threshold OR sort_order <= previous_sort_order)
    ) OR EXISTS (
        SELECT 1
        FROM membership_tiers
        WHERE active AND sort_order < 0
    ) THEN
        RAISE EXCEPTION 'V12 membership tier migration failed: active thresholds and sort orders must be strictly increasing';
    END IF;
END $$;

ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS tier_id BIGINT;

ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE coupons
    ADD COLUMN IF NOT EXISTS required_tier_id BIGINT;

UPDATE app_users users
SET tier_id = tiers.id
FROM membership_tiers tiers
WHERE users.tier::text = tiers.code
  AND users.tier_id IS NULL;

UPDATE coupons coupons
SET required_tier_id = tiers.id
FROM membership_tiers tiers
WHERE coupons.required_tier::text = tiers.code
  AND coupons.required_tier_id IS NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM app_users
        WHERE tier IS NOT NULL AND tier_id IS NULL
    ) THEN
        RAISE EXCEPTION 'V12 membership tier migration failed: an app_users.tier value has no membership_tiers mapping';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM coupons
        WHERE required_tier IS NOT NULL AND required_tier_id IS NULL
    ) THEN
        RAISE EXCEPTION 'V12 membership tier migration failed: a coupons.required_tier value has no membership_tiers mapping';
    END IF;
END $$;

ALTER TABLE app_users
    ADD CONSTRAINT fk_app_users_membership_tier
        FOREIGN KEY (tier_id) REFERENCES membership_tiers (id);

ALTER TABLE coupons
    ADD CONSTRAINT fk_coupons_required_membership_tier
        FOREIGN KEY (required_tier_id) REFERENCES membership_tiers (id);

CREATE INDEX idx_app_users_tier_id ON app_users (tier_id);
CREATE INDEX idx_coupons_required_tier_id ON coupons (required_tier_id);
