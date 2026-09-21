INSERT INTO tb_payment (
    employee_id,
    employee_name,
    daily_income,
    days_worked,
    gross_amount,
    payment_date,
    reference_date,
    status,
    type,
    created_at,
    updated_at
) VALUES

-- João Silva
(1, 'João Silva', 150.00, 20, 3000.00, '2026-07-31 12:00:00-03',
    '2026-07-01', 'PAID','SALARY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'João Silva', 150.00, 20, 3000.00, '2026-08-31 12:00:00-03',
    '2026-08-01', 'PAID', 'SALARY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Maria Santos
(2, 'Maria Santos', 180.00, 22, 3960.00, '2026-07-31 12:00:00-03',
    '2026-07-01', 'PAID', 'SALARY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2,'Maria Santos', 180.00, 22, 3960.00, NULL, '2026-08-01',
    'PENDING', 'SALARY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Carlos Oliveira
(3, 'Carlos Oliveira', 200.00, 20, 4000.00, '2026-07-31 12:00:00-03',
    '2026-07-01', 'PAID', 'SALARY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Carlos Oliveira', 200.00, 20, 4000.00, NULL,
    '2026-08-01', 'FAILED', 'SALARY',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Ana Costa
(4, 'Ana Costa', 170.00, 22, 3740.00, '2026-07-31 12:00:00-03',
    '2026-07-01', 'PAID', 'SALARY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Ana Costa', 170.00, 15, 2550.00, '2026-08-15 12:00:00-03',
    '2026-08-01', 'PAID', 'VACATION', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Pedro Almeida
(5, 'Pedro Almeida', 220.00, 20, 4400.00, '2026-07-31 12:00:00-03',
    '2026-07-01',  'PAID', 'SALARY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Pedro Almeida', 220.00, 20, 4400.00, NULL, '2026-08-01',
    'PENDING', 'THIRTEENTH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);