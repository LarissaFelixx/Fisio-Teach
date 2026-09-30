package com.app.fisiotech.consulta.dto;

import com.app.fisiotech.consulta.entity.Diagnostico;
import jakarta.validation.constraints.Size;

public record DiagnosticoDto(
        @Size(max = 2000, message = "O plano de tratamento deve ter no máximo 2000 caracteres")
        String planoTratamento,

        @Size(max = 2000, message = "Os objetivos do tratamento devem ter no máximo 2000 caracteres")
        String objetivosTratamento
) {

    public static DiagnosticoDto fromEntity(Diagnostico diagnostico) {
        return new DiagnosticoDto(
                diagnostico.getPlanoTratamento(),
                diagnostico.getObjetivosTratamento()
        );
    }

    public Diagnostico toEntity() {
        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setPlanoTratamento(planoTratamento);
        diagnostico.setObjetivosTratamento(objetivosTratamento);
        return diagnostico;
    }
}
