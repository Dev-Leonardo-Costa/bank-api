CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,

    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,

    amount NUMERIC(19,2) NOT NULL,

    source_account_id BIGINT,
    destination_account_id BIGINT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transaction_source_account
        FOREIGN KEY (source_account_id)
        REFERENCES accounts(id),

    CONSTRAINT fk_transaction_destination_account
        FOREIGN KEY (destination_account_id)
        REFERENCES accounts(id),

    CONSTRAINT chk_transaction_amount_positive
        CHECK (amount > 0)
);

CREATE INDEX idx_transaction_source_account
    ON transactions(source_account_id);

CREATE INDEX idx_transaction_destination_account
    ON transactions(destination_account_id);

CREATE INDEX idx_transaction_created_at
    ON transactions(created_at);