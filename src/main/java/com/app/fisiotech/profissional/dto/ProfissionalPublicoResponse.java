package com.app.fisiotech.profissional.dto;

import com.app.fisiotech.profissional.entity.Profissional;

import java.math.BigDecimal;
import java.util.List;

public record ProfissionalPublicoResponse(
        Long id,
        String nome,
        String especialidade,
        BigDecimal valorConsultaParticular,
        List<String> conveniosAceitos
) {

    public static ProfissionalPublicoResponse fromEntity(Profissional profissional) {
        return new ProfissionalPublicoResponse(
                profissional.getId(),
                profissional.getNome(),
                profissional.getEspecialidade(),
                profissional.getValorConsultaParticular(),
                profissional.getConveniosAceitos()
        );
    }
}
