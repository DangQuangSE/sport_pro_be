CREATE TABLE notification_outbox (
    id BIGSERIAL PRIMARY KEY,
    notification_key VARCHAR(160) NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    order_id BIGINT,
    source_outbox_id BIGINT,
    payload_snapshot JSONB NOT NULL,
    format_version INTEGER NOT NULL DEFAULT 1,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lease_owner VARCHAR(100),
    lease_expires_at TIMESTAMPTZ,
    next_chunk_index INTEGER NOT NULL DEFAULT 0,
    chunk_count INTEGER,
    last_error_category VARCHAR(60),
    last_error VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,

    CONSTRAINT fk_notification_outbox_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE RESTRICT,
    CONSTRAINT fk_notification_outbox_source
        FOREIGN KEY (source_outbox_id) REFERENCES notification_outbox (id) ON DELETE RESTRICT,
    CONSTRAINT uq_notification_outbox_key UNIQUE (notification_key),
    CONSTRAINT ck_notification_outbox_type
        CHECK (notification_type IN ('NEW_ORDER', 'DELIVERY_FAILURE_ALERT')),
    CONSTRAINT ck_notification_outbox_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED')),
    CONSTRAINT ck_notification_outbox_attempt_count
        CHECK (attempt_count BETWEEN 0 AND 6),
    CONSTRAINT ck_notification_outbox_chunk_progress
        CHECK (next_chunk_index >= 0 AND (chunk_count IS NULL OR
               (chunk_count >= 0 AND next_chunk_index <= chunk_count))),
    CONSTRAINT ck_notification_outbox_shape
        CHECK ((notification_type = 'NEW_ORDER' AND order_id IS NOT NULL AND source_outbox_id IS NULL)
            OR (notification_type = 'DELIVERY_FAILURE_ALERT' AND source_outbox_id IS NOT NULL))
);

CREATE UNIQUE INDEX uq_notification_outbox_new_order
    ON notification_outbox (notification_type, order_id)
    WHERE notification_type = 'NEW_ORDER';

CREATE UNIQUE INDEX uq_notification_outbox_failure_alert
    ON notification_outbox (notification_type, source_outbox_id)
    WHERE notification_type = 'DELIVERY_FAILURE_ALERT';

CREATE INDEX idx_notification_outbox_due
    ON notification_outbox (status, next_attempt_at, lease_expires_at)
    WHERE status IN ('PENDING', 'PROCESSING');

CREATE INDEX idx_notification_outbox_sent_retention
    ON notification_outbox (sent_at)
    WHERE status = 'SENT';

CREATE INDEX idx_notification_outbox_failed_retention
    ON notification_outbox (failed_at)
    WHERE status = 'FAILED';
