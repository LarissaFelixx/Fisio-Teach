package com.app.fisiotech.mensagem.dto;

import com.app.fisiotech.mensagem.entity.AutorMensagem;
import com.app.fisiotech.mensagem.entity.Mensagem;

import java.time.LocalDateTime;

public record MensagemResponse(
        Long id,
        Long pacienteId,
        Long profissionalId,
        AutorMensagem autor,
        String conteudo,
        LocalDateTime dataEnvio
) {

    public static MensagemResponse fromEntity(Mensagem mensagem) {
        return new MensagemResponse(
                mensagem.getId(),
                mensagem.getPaciente().getId(),
                mensagem.getProfissional().getId(),
                mensagem.getAutor(),
                mensagem.getConteudo(),
                mensagem.getDataEnvio()
        );
    }
}
