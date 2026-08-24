CREATE TABLE pix_schedules (
    id BIGSERIAL PRIMARY KEY,

    source_account_id BIGINT NOT NULL,

    pix_key VARCHAR(255) NOT NULL,

    amount NUMERIC(19,2) NOT NULL,

    scheduled_at TIMESTAMP NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pix_schedules_source_account
        FOREIGN KEY (source_account_id)
        REFERENCES accounts(id)
);

CREATE INDEX idx_pix_schedules_source_account_id
    ON pix_schedules(source_account_id);

CREATE INDEX idx_pix_schedules_status
    ON pix_schedules(status);

CREATE INDEX idx_pix_schedules_scheduled_at
    ON pix_schedules(scheduled_at);