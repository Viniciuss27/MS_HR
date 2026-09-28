INSERT INTO tb_vacation_request (
    employee_id, start_date, end_date, days_requested, status,
    requested_at, decided_at, decided_by,
    acquisition_start_date, acquisition_end_date, unjustified_absences
)
VALUES
( 1, '2025-10-01', '2025-10-15', 15, 'SOLICITADA', '2025-09-01T10:00:00Z',
    NULL, NULL, '2024-01-10', '2025-01-09', 0),
( 2, '2026-01-05', '2026-01-16', 12, 'APROVADA', '2025-12-01T10:00:00Z',
    '2025-12-05T14:30:00Z', 4, '2024-06-01', '2025-05-31', 0),
( 3, '2026-07-01', '2026-07-10', 10, 'REPROVADA', '2026-06-01T09:00:00Z',
    '2026-06-03T15:00:00Z', 4, '2025-03-20', '2026-03-19', 0);