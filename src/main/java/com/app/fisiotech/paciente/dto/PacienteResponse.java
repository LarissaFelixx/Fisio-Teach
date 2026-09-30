package com.app.fisiotech.paciente.dto;

import com.app.fisiotech.paciente.entity.Paciente;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteResponse(
        Long id,
        String nome,
        String email,
        Long profissionalId,
        String profissionalNome,
        LocalDate dataNascimento,
        String sexo,
        String profissao,
        String telefone,
        String endereco,
        String bairro,
        String foto,
        LocalDateTime dataCriacao
) {

    public static PacienteResponse fromEntity(Paciente paciente){
        Long profissionalId = paciente.getProfissional() != null ? paciente.getProfissional().getId() : null;
        String profissionalNome = paciente.getProfissional() != null ? paciente.getProfissional().getNome() : null;

        return new PacienteResponse(
                paciente.getId(),
                paciente.getNome(),
                paciente.getEmail(),
                profissionalId,
                profissionalNome,
                paciente.getDataNascimento(),
                paciente.getSexo(),
                paciente.getProfissao(),
                paciente.getTelefone(),
                paciente.getEndereco(),
                paciente.getBairro(),
                paciente.getFoto(),
                paciente.getDataCriacao()
        );
    }
}
