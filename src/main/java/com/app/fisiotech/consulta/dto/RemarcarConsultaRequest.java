package com.app.fisiotech.consulta.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RemarcarConsultaRequest(
        @NotNull(message = "A nova data e hora são obrigatórias")
        LocalDateTime novaDataHora
) {
}
