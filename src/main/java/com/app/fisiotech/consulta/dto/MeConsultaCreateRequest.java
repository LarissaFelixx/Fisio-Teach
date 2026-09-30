package com.app.fisiotech.consulta.dto;

import com.app.fisiotech.consulta.entity.TipoConsulta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record MeConsultaCreateRequest(
        @NotNull(message = "O profissional é obrigatório")
        Long profissionalId,

        @NotNull(message = "A data e hora são obrigatórias")
        LocalDateTime dataHora,

        @NotNull(message = "O tipo da consulta é obrigatório")
        TipoConsulta tipo,

        @Size(max = 60, message = "O convênio deve ter no máximo 60 caracteres")
        String convenio
) {
}
