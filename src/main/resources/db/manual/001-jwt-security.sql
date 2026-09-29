-- MySQL 8: execute once, with application stopped, before starting the JWT release.
-- First run the audit in 000-audit-emails.sql and resolve all returned conflicts.
CREATE TABLE auth_emails (
    subject VARCHAR(80) NOT NULL PRIMARY KEY,
    email VARCHAR(120) NOT NULL,
    CONSTRAINT uk_auth_email UNIQUE (email)
);

CREATE TABLE auth_sessions (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    subject VARCHAR(80) NOT NULL,
    credential_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked BIT NOT NULL,
    INDEX idx_auth_session_subject (subject)
);

CREATE TABLE auth_refresh_tokens (
    token_hash VARCHAR(64) NOT NULL PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    consumed BIT NOT NULL,
    INDEX idx_refresh_session (session_id)
);

-- The unique email constraint rejects ambiguous legacy accounts.
INSERT INTO auth_emails (subject, email)
SELECT CONCAT('ADMIN:', id), LOWER(TRIM(email)) FROM admins
UNION ALL
SELECT CONCAT('PROFISSIONAL:', id), LOWER(TRIM(email)) FROM profissionais
UNION ALL
SELECT CONCAT('PACIENTE:', id), LOWER(TRIM(email)) FROM pacientes;
