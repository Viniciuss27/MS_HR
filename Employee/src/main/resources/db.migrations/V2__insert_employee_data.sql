INSERT INTO tb_employee
    (id, name, cpf, position, birth_date, daily_income, active, hire_date,
     created_at, updated_at)
VALUES
    (1, 'João Silva', '11111111111', 'COLABORADOR', '1995-03-15',
     100.00, true, '2024-01-10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (2, 'Maria Santos', '22222222222', 'ANALISTA RH', '1990-07-22',
     150.00, true, '2023-06-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (3, 'Carlos Oliveira', '33333333333', 'COLABORADOR', '1998-11-05',
     90.00, true, '2024-03-20', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (4, 'Ana Costa', '44444444444', 'GERENTE', '1985-01-30',
     250.00, true, '2020-08-15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (5, 'Pedro Souza', '55555555555', 'COLABORADOR', '1997-09-12',
     110.00, true, '2024-05-10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);