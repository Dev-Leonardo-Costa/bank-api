CREATE TABLE pix_idempotency (
    id BIGSERIAL PRIMARY KEY,

    idempotency_key VARCHAR(100) NOT NULL,

    transaction_id BIGINT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_pix_idempotency_key
        UNIQUE (idempotency_key),

    CONSTRAINT fk_pix_idempotency_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
);