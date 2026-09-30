package com.app.fisiotech.mensagem.dto;

import com.app.fisiotech.mensagem.entity.AutorMensagem;
import com.app.fisiotech.mensagem.entity.Mensagem;
import com.app.fisiotech.profissional.entity.Profissional;

import java.time.LocalDateTime;

public record MinhaConversaResponse(
        Long profissionalId,
        String profissionalNome,
        String ultimaMensagem,
        AutorMensagem ultimoAutor,
        LocalDateTime dataUltimaMensagem
) {

    public static MinhaConversaResponse from(Profissional profissional, Mensagem ultimaMensagem) {
        return new MinhaConversaResponse(
                profissional.getId(),
                profissional.getNome(),
                ultimaMensagem != null ? ultimaMensagem.getConteudo() : null,
                ultimaMensagem != null ? ultimaMensagem.getAutor() : null,
                ultimaMensagem != null ? ultimaMensagem.getDataEnvio() : null
        );
    }
}
