CREATE TABLE pix_keys (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    key_value VARCHAR(255) NOT NULL,
    account_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_pix_keys_key_value
        UNIQUE (key_value),

    CONSTRAINT fk_pix_keys_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_pix_keys_account_id
    ON pix_keys(account_id);

CREATE INDEX idx_pix_keys_type
    ON pix_keys(type);