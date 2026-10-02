CREATE TABLE auth_emails (
    subject VARCHAR(80) PRIMARY KEY,
    email VARCHAR(120) NOT NULL,
    CONSTRAINT uk_auth_email UNIQUE (email)
);

CREATE TABLE auth_sessions (
    id VARCHAR(36) PRIMARY KEY,
    subject VARCHAR(80) NOT NULL,
    credential_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    revoked BOOLEAN NOT NULL
);
CREATE INDEX idx_auth_session_subject ON auth_sessions(subject);

CREATE TABLE auth_refresh_tokens (
    token_hash VARCHAR(64) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    consumed BOOLEAN NOT NULL
);
CREATE INDEX idx_refresh_session ON auth_refresh_tokens(session_id);

INSERT INTO auth_emails (subject, email)
SELECT CONCAT('ADMIN:', id), LOWER(TRIM(email)) FROM admins;
INSERT INTO auth_emails (subject, email)
SELECT CONCAT('PROFISSIONAL:', id), LOWER(TRIM(email)) FROM profissionais;
INSERT INTO auth_emails (subject, email)
SELECT CONCAT('PACIENTE:', id), LOWER(TRIM(email)) FROM pacientes;
