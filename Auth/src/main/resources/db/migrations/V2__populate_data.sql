-- ROLES
INSERT INTO tb_role (role_name) VALUES
('USER'),
('ADMIN'),
('HR'),
('MANAGER');


-- USERS
INSERT INTO tb_user (name, email, password, employee_id) VALUES
('Administrador', 'admin@hr.com',
 '$2b$10$0LgsseBfk6N7LX80dCF6TO8/2Tup6iIQsK88axNXd5MWWxFQFPYFu', 1),

('Recursos Humanos', 'hr@hr.com',
 '$2b$10$0LgsseBfk6N7LX80dCF6TO8/2Tup6iIQsK88axNXd5MWWxFQFPYFu', 2),

('Gerente', 'manager@hr.com',
 '$2b$10$0LgsseBfk6N7LX80dCF6TO8/2Tup6iIQsK88axNXd5MWWxFQFPYFu', 3),

('Usuário', 'user@hr.com',
 '$2b$10$0LgsseBfk6N7LX80dCF6TO8/2Tup6iIQsK88axNXd5MWWxFQFPYFu', 4);

-- todas senhas 123456 para teste

-- ADMIN
INSERT INTO tb_user_role (user_id, role_id)
SELECT u.id, r.id
FROM tb_user u
CROSS JOIN tb_role r
WHERE u.email = 'admin@hr.com'
  AND r.role_name = 'ADMIN';


-- HR
INSERT INTO tb_user_role (user_id, role_id)
SELECT u.id, r.id
FROM tb_user u
CROSS JOIN tb_role r
WHERE u.email = 'hr@hr.com'
  AND r.role_name = 'HR';


-- MANAGER + USER
INSERT INTO tb_user_role (user_id, role_id)
SELECT u.id, r.id
FROM tb_user u
CROSS JOIN tb_role r
WHERE u.email = 'manager@hr.com'
  AND r.role_name IN ('MANAGER', 'USER');


-- USER
INSERT INTO tb_user_role (user_id, role_id)
SELECT u.id, r.id
FROM tb_user u
CROSS JOIN tb_role r
WHERE u.email = 'user@hr.com'
  AND r.role_name = 'USER';