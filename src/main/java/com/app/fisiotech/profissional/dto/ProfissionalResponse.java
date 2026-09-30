package com.app.fisiotech.profissional.dto;

import com.app.fisiotech.profissional.entity.Profissional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProfissionalResponse(
        Long id,
        String nome,
        String email,
        String registroProfissional,
        String especialidade,
        BigDecimal valorConsultaParticular,
        List<String> conveniosAceitos,
        String foto,
        LocalDate dataNascimento,
        String sexo,
        String telefone,
        LocalDateTime dataCriacao
) {

    public static ProfissionalResponse fromEntity(Profissional profissional) {
        return new ProfissionalResponse(
                profissional.getId(),
                profissional.getNome(),
                profissional.getEmail(),
                profissional.getRegistroProfissional(),
                profissional.getEspecialidade(),
                profissional.getValorConsultaParticular(),
                profissional.getConveniosAceitos(),
                profissional.getFoto(),
                profissional.getDataNascimento(),
                profissional.getSexo(),
                profissional.getTelefone(),
                profissional.getDataCriacao()
        );
    }
}
