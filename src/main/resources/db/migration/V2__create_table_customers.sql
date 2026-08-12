CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,

    full_name VARCHAR(150) NOT NULL,

    cpf VARCHAR(255),

    email VARCHAR(150) NOT NULL UNIQUE,

    active BOOLEAN NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP
);