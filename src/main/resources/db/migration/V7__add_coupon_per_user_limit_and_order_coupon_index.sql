ALTER TABLE coupons ADD COLUMN max_usage_per_user INTEGER;

CREATE INDEX idx_order_coupon_id ON orders (coupon_id);
