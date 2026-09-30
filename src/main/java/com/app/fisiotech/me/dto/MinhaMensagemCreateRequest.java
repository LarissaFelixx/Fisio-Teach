package com.app.fisiotech.me.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MinhaMensagemCreateRequest(
        @NotBlank(message = "O conteúdo é obrigatório")
        @Size(max = 2000, message = "O conteúdo deve ter no máximo 2000 caracteres")
        String conteudo
) {
}
