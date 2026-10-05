INSERT INTO membership_tier_configs (tier, threshold, description)
VALUES
    ('BRONZE', 0, 'Base membership tier'),
    ('SILVER', 5000000, 'Silver membership tier'),
    ('GOLD', 15000000, 'Gold membership tier'),
    ('PLATINUM', 30000000, 'Platinum membership tier')
ON CONFLICT (tier) DO NOTHING;
