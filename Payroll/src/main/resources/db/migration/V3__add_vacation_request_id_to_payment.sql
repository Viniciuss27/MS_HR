ALTER TABLE tb_payment
    ADD COLUMN vacation_request_id BIGINT;

ALTER TABLE tb_payment
    ADD CONSTRAINT uq_payment_vacation_request_id UNIQUE (vacation_request_id);

CREATE INDEX idx_payment_vacation_request_id
    ON tb_payment (vacation_request_id);