ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS subtotal_amount NUMERIC(15, 2),
    ADD COLUMN IF NOT EXISTS printing_amount NUMERIC(15, 2),
    ADD COLUMN IF NOT EXISTS tier_discount_amount NUMERIC(15, 2),
    ADD COLUMN IF NOT EXISTS coupon_discount_amount NUMERIC(15, 2),
    ADD COLUMN IF NOT EXISTS shipping_amount NUMERIC(15, 2),
    ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(15, 2),
    ADD COLUMN IF NOT EXISTS applied_tier_code VARCHAR(40),
    ADD COLUMN IF NOT EXISTS pricing_rule_versions JSONB,
    ADD COLUMN IF NOT EXISTS pricing_snapshot_status VARCHAR(20) NOT NULL DEFAULT 'LEGACY';

UPDATE orders
SET pricing_snapshot_status = 'LEGACY'
WHERE pricing_snapshot_status IS NULL;

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_pricing_snapshot_status
    CHECK (pricing_snapshot_status IN ('LEGACY', 'COMPLETE'));

ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS unit_price NUMERIC(15, 2),
    ADD COLUMN IF NOT EXISTS printing_amount NUMERIC(15, 2);

-- Existing orders do not have enough information to reconstruct shipping,
-- tax, or tier/coupon components. Keep them explicitly legacy.
UPDATE orders
SET pricing_snapshot_status = 'LEGACY'
WHERE pricing_snapshot_status <> 'COMPLETE';

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_complete_pricing_snapshot
    CHECK (
        pricing_snapshot_status = 'LEGACY'
        OR (
            subtotal_amount IS NOT NULL
            AND printing_amount IS NOT NULL
            AND tier_discount_amount IS NOT NULL
            AND coupon_discount_amount IS NOT NULL
            AND shipping_amount IS NOT NULL
            AND tax_amount IS NOT NULL
            AND applied_tier_code IS NOT NULL
            AND pricing_rule_versions IS NOT NULL
        )
    );

CREATE INDEX IF NOT EXISTS idx_orders_pricing_snapshot_status
    ON orders (pricing_snapshot_status);
