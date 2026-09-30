CREATE TABLE password_reset_tokens (
    token_hash VARCHAR(64) PRIMARY KEY,
    subject VARCHAR(80) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    consumed BOOLEAN NOT NULL
);
CREATE INDEX idx_password_reset_subject_created ON password_reset_tokens(subject, created_at);
