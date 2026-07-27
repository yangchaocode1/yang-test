CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    real_name   VARCHAR(50),
    phone       VARCHAR(20),
    email       VARCHAR(100),
    avatar      VARCHAR(500),
    status      TINYINT      NOT NULL DEFAULT 1,
    expire_time TIMESTAMP,
    must_change_password TINYINT NOT NULL DEFAULT 0,
    created_by  BIGINT,
    created_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  BIGINT,
    updated_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code   VARCHAR(50)  NOT NULL UNIQUE,
    role_name   VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    parent_id   BIGINT       DEFAULT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_permission (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    permission_name VARCHAR(100) NOT NULL,
    permission_type VARCHAR(20)  NOT NULL,
    parent_id       BIGINT       DEFAULT NULL,
    path            VARCHAR(255),
    icon            VARCHAR(100),
    sort_order      INT          NOT NULL DEFAULT 0,
    status          TINYINT      NOT NULL DEFAULT 1,
    created_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS sys_role_inheritance (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_role_id BIGINT NOT NULL,
    child_role_id  BIGINT NOT NULL,
    UNIQUE (parent_role_id, child_role_id)
);

CREATE TABLE IF NOT EXISTS biz_data_model (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_code  VARCHAR(50)  NOT NULL UNIQUE,
    model_name  VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    table_name  VARCHAR(100) NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS biz_data_field (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id            BIGINT       NOT NULL,
    field_code          VARCHAR(50)  NOT NULL,
    field_name          VARCHAR(100) NOT NULL,
    field_type          VARCHAR(50)  NOT NULL,
    required            TINYINT      NOT NULL DEFAULT 0,
    unique_flag         TINYINT      NOT NULL DEFAULT 0,
    reference_model_id  BIGINT       DEFAULT NULL,
    sort_order          INT          NOT NULL DEFAULT 0,
    options             CLOB,
    created_time        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS biz_data_record (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id    BIGINT       NOT NULL,
    data_json   CLOB,
    created_by  BIGINT,
    created_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  BIGINT,
    updated_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_config (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key  VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    config_type VARCHAR(20)  NOT NULL,
    description VARCHAR(500),
    created_time TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_audit_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT,
    username    VARCHAR(50),
    operation   VARCHAR(100) NOT NULL,
    module      VARCHAR(50),
    target_type VARCHAR(50),
    target_id   VARCHAR(100),
    old_value   TEXT,
    new_value   TEXT,
    ip_address  VARCHAR(50),
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT,
    username    VARCHAR(50),
    operation   VARCHAR(100) NOT NULL,
    method      VARCHAR(255),
    params      TEXT,
    result      TEXT,
    ip_address  VARCHAR(50),
    duration    BIGINT,
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
