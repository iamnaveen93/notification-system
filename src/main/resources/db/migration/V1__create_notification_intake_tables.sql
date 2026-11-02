CREATE TABLE notifications (
    notification_id VARCHAR(64) NOT NULL,
    template_id VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    scheduled_at TIMESTAMP NULL,
    correlation_id VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    PRIMARY KEY (notification_id)
);

CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(128) NOT NULL,
    request_hash CHAR(64) NOT NULL,
    notification_id VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (idempotency_key),
    CONSTRAINT fk_idempotency_keys_notification
        FOREIGN KEY (notification_id) REFERENCES notifications (notification_id)
);

CREATE INDEX idx_idempotency_keys_expires_at ON idempotency_keys (expires_at);
