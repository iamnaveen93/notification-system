CREATE TABLE campaigns (
    campaign_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    template_id VARCHAR(255) NOT NULL,
    state VARCHAR(32) NOT NULL,
    audience_segment_id VARCHAR(255) NULL,
    audience_file_ref VARCHAR(1024) NULL,
    channels VARCHAR(64) NOT NULL,
    variables TEXT NULL,
    locale VARCHAR(32) NULL,
    start_at TIMESTAMP NOT NULL,
    end_by TIMESTAMP NULL,
    max_per_second INT NULL,
    started_at TIMESTAMP NULL,
    processed_offset BIGINT NOT NULL DEFAULT 0,
    audience_size BIGINT NOT NULL DEFAULT 0,
    queued BIGINT NOT NULL DEFAULT 0,
    sent BIGINT NOT NULL DEFAULT 0,
    delivered BIGINT NOT NULL DEFAULT 0,
    failed BIGINT NOT NULL DEFAULT 0,
    suppressed BIGINT NOT NULL DEFAULT 0,
    expired BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    PRIMARY KEY (campaign_id)
);

CREATE INDEX idx_campaigns_state_start_at ON campaigns (state, start_at);

CREATE TABLE campaign_idempotency_keys (
    idempotency_key VARCHAR(128) NOT NULL,
    request_hash CHAR(64) NOT NULL,
    campaign_id VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (idempotency_key),
    CONSTRAINT fk_campaign_idempotency_keys_campaign
        FOREIGN KEY (campaign_id) REFERENCES campaigns (campaign_id)
);

CREATE INDEX idx_campaign_idempotency_keys_expires_at ON campaign_idempotency_keys (expires_at);
