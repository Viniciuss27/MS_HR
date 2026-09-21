CREATE TABLE tb_payment (
    id BIGSERIAL PRIMARY KEY,

    employee_id BIGINT NOT NULL,

    employee_name VARCHAR(100) NOT NULL,

    daily_income NUMERIC(19, 2) NOT NULL,

    days_worked INTEGER NOT NULL,

    gross_amount NUMERIC(19, 2) NOT NULL,

    payment_date TIMESTAMP WITH TIME ZONE,

    reference_date DATE NOT NULL,

    status VARCHAR(20) NOT NULL,

    type VARCHAR(20) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);