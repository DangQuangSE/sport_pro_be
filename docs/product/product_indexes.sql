CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_product_brand ON products(brand_id);
CREATE INDEX idx_product_gender ON products(gender);
CREATE INDEX idx_product_status ON products(status);
CREATE INDEX idx_product_slug ON products(slug);

CREATE INDEX idx_variant_product ON product_variants(product_id);
CREATE INDEX idx_variant_size ON product_variants(size);
CREATE INDEX idx_variant_color ON product_variants(color);
CREATE INDEX idx_variant_price ON product_variants(price);
CREATE INDEX idx_variant_stock ON product_variants(stock_quantity);

-- Composite Indexes cho tính năng Filter
CREATE INDEX idx_product_filter ON products(category_id, brand_id, gender, status);
CREATE INDEX idx_variant_filter ON product_variants(size, color, price);
