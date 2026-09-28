CREATE TABLE tb_vacation_request (
    id BIGSERIAL PRIMARY KEY,

    employee_id BIGINT NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days_requested INTEGER NOT NULL,

    status VARCHAR(20) NOT NULL,

    requested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decided_at TIMESTAMP WITH TIME ZONE,

    decided_by BIGINT,

    acquisition_start_date DATE NOT NULL,
    acquisition_end_date DATE NOT NULL,

    unjustified_absences INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_vacation_request_employee
        FOREIGN KEY (employee_id)
        REFERENCES tb_employee(id),

    CONSTRAINT ck_vacation_request_days
        CHECK (days_requested > 0),

    CONSTRAINT ck_vacation_request_dates
        CHECK (end_date >= start_date),

    CONSTRAINT ck_vacation_request_absences
        CHECK (unjustified_absences >= 0)
);

CREATE INDEX idx_vacation_request_employee
    ON tb_vacation_request(employee_id);

CREATE INDEX idx_vacation_request_acquisition
    ON tb_vacation_request(employee_id, acquisition_start_date);

CREATE INDEX idx_vacation_request_status
    ON tb_vacation_request(status);