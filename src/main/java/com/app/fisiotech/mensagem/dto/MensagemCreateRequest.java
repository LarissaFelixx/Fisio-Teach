package com.app.fisiotech.mensagem.dto;

import com.app.fisiotech.mensagem.entity.AutorMensagem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MensagemCreateRequest(
        @NotNull(message = "O paciente é obrigatório")
        Long pacienteId,

        @NotNull(message = "O autor é obrigatório")
        AutorMensagem autor,

        @NotBlank(message = "O conteúdo é obrigatório")
        @Size(max = 2000, message = "O conteúdo deve ter no máximo 2000 caracteres")
        String conteudo
) {
}
