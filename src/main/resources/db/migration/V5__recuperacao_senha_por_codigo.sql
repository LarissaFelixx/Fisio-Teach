-- A recuperação de senha passou a usar código de 6 dígitos enviado por email
-- (RecuperacaoSenhaService) no lugar do link com token da V3. Os tokens pendentes
-- são descartáveis: quem estava no meio do fluxo só precisa pedir um código novo.
DROP TABLE password_reset_tokens;

CREATE TABLE codigos_recuperacao_senha (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(120) NOT NULL,
    codigo_hash VARCHAR(255) NOT NULL,
    expira_em TIMESTAMP(6) NOT NULL,
    tentativas INT NOT NULL,
    usado BOOLEAN NOT NULL,
    data_criacao TIMESTAMP(6) NOT NULL
);
CREATE INDEX idx_codigo_recuperacao_email ON codigos_recuperacao_senha(email);
