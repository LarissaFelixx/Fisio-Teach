package com.app.fisiotech.consulta.dto;

import com.app.fisiotech.consulta.entity.HabitosVida;
import jakarta.validation.constraints.Size;

public record HabitosVidaDto(
        @Size(max = 2000, message = "A atividade física deve ter no máximo 2000 caracteres")
        String atividadeFisica,

        @Size(max = 2000, message = "A rotina de trabalho deve ter no máximo 2000 caracteres")
        String rotinaTrabalho,

        Boolean tabagismo,

        Boolean consumoAlcool
) {

    public static HabitosVidaDto fromEntity(HabitosVida habitosVida) {
        return new HabitosVidaDto(
                habitosVida.getAtividadeFisica(),
                habitosVida.getRotinaTrabalho(),
                habitosVida.getTabagismo(),
                habitosVida.getConsumoAlcool()
        );
    }

    public HabitosVida toEntity() {
        HabitosVida habitosVida = new HabitosVida();
        habitosVida.setAtividadeFisica(atividadeFisica);
        habitosVida.setRotinaTrabalho(rotinaTrabalho);
        habitosVida.setTabagismo(tabagismo);
        habitosVida.setConsumoAlcool(consumoAlcool);
        return habitosVida;
    }
}
