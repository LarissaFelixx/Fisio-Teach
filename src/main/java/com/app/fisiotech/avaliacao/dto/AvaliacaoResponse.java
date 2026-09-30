package com.app.fisiotech.avaliacao.dto;

import com.app.fisiotech.avaliacao.entity.Avaliacao;

import java.time.LocalDateTime;

public record AvaliacaoResponse(
        Long id,
        Long consultaId,
        Integer nota,
        String comentario,
        LocalDateTime dataCriacao
) {

    public static AvaliacaoResponse fromEntity(Avaliacao avaliacao) {
        return new AvaliacaoResponse(
                avaliacao.getId(),
                avaliacao.getConsulta().getId(),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getDataCriacao()
        );
    }
}
