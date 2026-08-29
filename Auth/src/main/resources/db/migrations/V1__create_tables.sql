CREATE TABLE tb_user (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE tb_role (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE tb_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id)
        REFERENCES tb_user(id),

    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id)
        REFERENCES tb_role(id)
);