package com.app.fisiotech.consulta.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DisponibilidadeResponse(
        LocalDate data,
        List<SlotDisponibilidade> horarios
) {

    public record SlotDisponibilidade(
            LocalTime horario,
            boolean disponivel
    ) {
    }
}
