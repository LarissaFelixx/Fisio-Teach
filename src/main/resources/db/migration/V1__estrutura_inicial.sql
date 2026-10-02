CREATE TABLE admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_admin_email UNIQUE (email)
);

CREATE TABLE profissionais (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    registro_profissional VARCHAR(20) NOT NULL,
    especialidade VARCHAR(120) NOT NULL,
    data_criacao TIMESTAMP(6) NOT NULL,
    valor_consulta_particular DECIMAL(10,2),
    foto VARCHAR(255),
    data_nascimento DATE,
    sexo VARCHAR(30),
    telefone VARCHAR(30),
    CONSTRAINT uk_profissional_email UNIQUE (email),
    CONSTRAINT uk_profissional_registro UNIQUE (registro_profissional)
);

CREATE TABLE profissional_convenios (
    profissional_id BIGINT NOT NULL,
    convenio VARCHAR(255),
    CONSTRAINT fk_convenio_profissional FOREIGN KEY (profissional_id) REFERENCES profissionais(id)
);

CREATE TABLE pacientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP(6) NOT NULL,
    profissional_id BIGINT,
    data_nascimento DATE,
    sexo VARCHAR(30),
    profissao VARCHAR(120),
    telefone VARCHAR(30),
    endereco VARCHAR(200),
    bairro VARCHAR(120),
    foto VARCHAR(255),
    CONSTRAINT uk_paciente_email UNIQUE (email),
    CONSTRAINT fk_paciente_profissional FOREIGN KEY (profissional_id) REFERENCES profissionais(id)
);

CREATE TABLE consultas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    profissional_id BIGINT NOT NULL,
    data_hora TIMESTAMP(6) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    convenio VARCHAR(60),
    valor DECIMAL(10,2),
    foi_remarcada BOOLEAN NOT NULL,
    qc_queixa_principal TEXT, qc_historia_doenca_atual TEXT,
    qc_historico_saude VARCHAR(255), qc_cirurgias BOOLEAN, qc_cirurgias_descricao TEXT,
    qc_lesoes_anteriores BOOLEAN, qc_lesoes_anteriores_descricao TEXT, qc_medicamentos TEXT,
    hv_atividade_fisica TEXT, hv_rotina_trabalho TEXT, hv_tabagismo BOOLEAN, hv_consumo_alcool BOOLEAN,
    ef_postura TEXT, ef_amplitude_movimento TEXT, ef_palpacao TEXT, ef_forca_muscular TEXT,
    dx_plano_tratamento TEXT, dx_objetivos_tratamento TEXT,
    data_criacao TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_consulta_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    CONSTRAINT fk_consulta_profissional FOREIGN KEY (profissional_id) REFERENCES profissionais(id)
);

CREATE INDEX idx_consulta_profissional_data ON consultas(profissional_id, data_hora);
CREATE INDEX idx_consulta_paciente_data ON consultas(paciente_id, data_hora);

CREATE TABLE mensagens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    profissional_id BIGINT NOT NULL,
    autor VARCHAR(20) NOT NULL,
    conteudo VARCHAR(2000) NOT NULL,
    data_envio TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_mensagem_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    CONSTRAINT fk_mensagem_profissional FOREIGN KEY (profissional_id) REFERENCES profissionais(id)
);

CREATE TABLE avaliacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consulta_id BIGINT NOT NULL,
    nota INTEGER NOT NULL,
    comentario VARCHAR(1000),
    data_criacao TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_avaliacao_consulta UNIQUE (consulta_id),
    CONSTRAINT fk_avaliacao_consulta FOREIGN KEY (consulta_id) REFERENCES consultas(id)
);
