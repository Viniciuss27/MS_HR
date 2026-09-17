CREATE TABLE tb_employee (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    position VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    daily_income NUMERIC(19, 2) NOT NULL,
    active BOOLEAN NOT NULL,
    hire_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);