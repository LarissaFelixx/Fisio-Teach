package com.app.fisiotech.consulta.dto;

import com.app.fisiotech.consulta.entity.Consulta;
import com.app.fisiotech.consulta.entity.StatusConsulta;
import com.app.fisiotech.consulta.entity.TipoConsulta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConsultaResponse(
        Long id,
        Long pacienteId,
        String pacienteNome,
        Long profissionalId,
        String profissionalNome,
        LocalDateTime dataHora,
        TipoConsulta tipo,
        StatusConsulta status,
        boolean foiRemarcada,
        String convenio,
        BigDecimal valor,
        QuadroClinicoDto quadroClinico,
        HabitosVidaDto habitosVida,
        ExameFisicoDto exameFisico,
        DiagnosticoDto diagnostico,
        LocalDateTime dataCriacao
) {

    public static ConsultaResponse fromEntity(Consulta consulta) {
        return new ConsultaResponse(
                consulta.getId(),
                consulta.getPaciente().getId(),
                consulta.getPaciente().getNome(),
                consulta.getProfissional().getId(),
                consulta.getProfissional().getNome(),
                consulta.getDataHora(),
                consulta.getTipo(),
                consulta.getStatus(),
                consulta.isFoiRemarcada(),
                consulta.getConvenio(),
                consulta.getValor(),
                QuadroClinicoDto.fromEntity(consulta.getQuadroClinico()),
                HabitosVidaDto.fromEntity(consulta.getHabitosVida()),
                ExameFisicoDto.fromEntity(consulta.getExameFisico()),
                DiagnosticoDto.fromEntity(consulta.getDiagnostico()),
                consulta.getDataCriacao()
        );
    }
}
