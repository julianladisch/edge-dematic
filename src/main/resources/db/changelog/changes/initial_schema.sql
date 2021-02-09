CREATE TABLE IF NOT EXISTS system_user_parameters
(
    id          UUID PRIMARY KEY,
    username    VARCHAR(50) NOT NULL,
    password    VARCHAR(50) NOT NULL,
    okapi_token VARCHAR(8000),
    okapi_url   VARCHAR(100),
    tenant_id   VARCHAR(100)
);
