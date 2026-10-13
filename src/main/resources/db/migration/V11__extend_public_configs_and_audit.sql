ALTER TABLE public_configs
    ADD COLUMN IF NOT EXISTS category VARCHAR(40) NOT NULL DEFAULT 'CONTENT',
    ADD COLUMN IF NOT EXISTS unit VARCHAR(20),
    ADD COLUMN IF NOT EXISTS scope VARCHAR(12) NOT NULL DEFAULT 'INTERNAL',
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS validation_rules TEXT,
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

ALTER TABLE public_configs
    ADD CONSTRAINT chk_public_configs_scope CHECK (scope IN ('PUBLIC', 'INTERNAL'));

-- Existing rows are not assumed to be safe for anonymous reads. Only the
-- explicitly supported public content key is promoted after the new columns
-- are present; all other rows remain internal and inactive.
UPDATE public_configs
SET scope = 'PUBLIC', active = TRUE
WHERE config_key IN ('zalo_link');

CREATE INDEX IF NOT EXISTS idx_public_configs_scope_active
    ON public_configs (scope, active);

CREATE INDEX IF NOT EXISTS idx_public_configs_category
    ON public_configs (category);

CREATE TABLE IF NOT EXISTS setting_change_audits (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL,
    actor_id BIGINT,
    version BIGINT NOT NULL,
    before_value TEXT,
    after_value TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_setting_audit_key
    ON setting_change_audits (setting_key);

CREATE INDEX IF NOT EXISTS idx_setting_audit_actor
    ON setting_change_audits (actor_id);

CREATE INDEX IF NOT EXISTS idx_setting_audit_changed_at
    ON setting_change_audits (changed_at);
