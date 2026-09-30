package com.app.fisiotech.consulta.dto;

import com.app.fisiotech.consulta.entity.ExameFisico;
import jakarta.validation.constraints.Size;

public record ExameFisicoDto(
        @Size(max = 2000, message = "A postura deve ter no máximo 2000 caracteres")
        String postura,

        @Size(max = 2000, message = "A amplitude de movimento deve ter no máximo 2000 caracteres")
        String amplitudeMovimento,

        @Size(max = 2000, message = "A palpação deve ter no máximo 2000 caracteres")
        String palpacao,

        @Size(max = 2000, message = "A força muscular deve ter no máximo 2000 caracteres")
        String forcaMuscular
) {

    public static ExameFisicoDto fromEntity(ExameFisico exameFisico) {
        return new ExameFisicoDto(
                exameFisico.getPostura(),
                exameFisico.getAmplitudeMovimento(),
                exameFisico.getPalpacao(),
                exameFisico.getForcaMuscular()
        );
    }

    public ExameFisico toEntity() {
        ExameFisico exameFisico = new ExameFisico();
        exameFisico.setPostura(postura);
        exameFisico.setAmplitudeMovimento(amplitudeMovimento);
        exameFisico.setPalpacao(palpacao);
        exameFisico.setForcaMuscular(forcaMuscular);
        return exameFisico;
    }
}
