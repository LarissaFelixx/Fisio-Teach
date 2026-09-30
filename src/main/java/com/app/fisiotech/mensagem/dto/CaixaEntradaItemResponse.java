package com.app.fisiotech.mensagem.dto;

import com.app.fisiotech.mensagem.entity.AutorMensagem;
import com.app.fisiotech.mensagem.entity.Mensagem;
import com.app.fisiotech.paciente.entity.Paciente;

import java.time.LocalDateTime;

public record CaixaEntradaItemResponse(
        Long pacienteId,
        String pacienteNome,
        String ultimaMensagem,
        AutorMensagem ultimoAutor,
        LocalDateTime dataUltimaMensagem
) {

    public static CaixaEntradaItemResponse from(Paciente paciente, Mensagem ultimaMensagem) {
        return new CaixaEntradaItemResponse(
                paciente.getId(),
                paciente.getNome(),
                ultimaMensagem != null ? ultimaMensagem.getConteudo() : null,
                ultimaMensagem != null ? ultimaMensagem.getAutor() : null,
                ultimaMensagem != null ? ultimaMensagem.getDataEnvio() : null
        );
    }
}
