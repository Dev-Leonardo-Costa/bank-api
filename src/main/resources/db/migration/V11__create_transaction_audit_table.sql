CREATE TABLE transaction_audit (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT,
    action VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    performed_by VARCHAR(150) NOT NULL,
    details VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transaction_audit_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
);