CREATE TABLE evolucoes_clinicas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    profissional_id BIGINT NOT NULL,
    consulta_id BIGINT,
    observacoes VARCHAR(4000) NOT NULL,
    procedimentos VARCHAR(4000),
    resposta_paciente VARCHAR(2000),
    conduta VARCHAR(2000),
    visivel_paciente BOOLEAN NOT NULL,
    data_registro TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_evolucao_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    CONSTRAINT fk_evolucao_profissional FOREIGN KEY (profissional_id) REFERENCES profissionais(id),
    CONSTRAINT fk_evolucao_consulta FOREIGN KEY (consulta_id) REFERENCES consultas(id)
);
CREATE INDEX idx_evolucao_paciente_data ON evolucoes_clinicas(paciente_id, data_registro);

CREATE TABLE planos_terapeuticos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    profissional_id BIGINT NOT NULL,
    revisao INTEGER NOT NULL,
    objetivos VARCHAR(4000) NOT NULL,
    condutas VARCHAR(4000) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim_prevista DATE,
    status VARCHAR(20) NOT NULL,
    visivel_paciente BOOLEAN NOT NULL,
    data_criacao TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_plano_revisao UNIQUE (paciente_id, revisao),
    CONSTRAINT fk_plano_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    CONSTRAINT fk_plano_profissional FOREIGN KEY (profissional_id) REFERENCES profissionais(id)
);
CREATE INDEX idx_plano_paciente_revisao ON planos_terapeuticos(paciente_id, revisao);
