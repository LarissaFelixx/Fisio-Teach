package com.app.fisiotech.consulta.dto;

import com.app.fisiotech.consulta.entity.StatusConsulta;
import com.app.fisiotech.consulta.entity.TipoConsulta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConsultaUpdateRequest(
        @NotNull(message = "A data e hora são obrigatórias")
        LocalDateTime dataHora,

        @NotNull(message = "O tipo da consulta é obrigatório")
        TipoConsulta tipo,

        @NotNull(message = "O status da consulta é obrigatório")
        StatusConsulta status,

        @Size(max = 60, message = "O convênio deve ter no máximo 60 caracteres")
        String convenio,

        @DecimalMin(value = "0.0", inclusive = true, message = "O valor não pode ser negativo")
        BigDecimal valor,

        @Valid
        QuadroClinicoDto quadroClinico,

        @Valid
        HabitosVidaDto habitosVida,

        @Valid
        ExameFisicoDto exameFisico,

        @Valid
        DiagnosticoDto diagnostico
) {
}
