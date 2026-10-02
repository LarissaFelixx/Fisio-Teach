package com.app.fisiotech.exception;

public class CodigoRecuperacaoInvalidoException extends RuntimeException {

    public CodigoRecuperacaoInvalidoException() {
        // Mensagem única de propósito: não revelamos se o email existe, se o código expirou
        // ou se as tentativas acabaram - tudo isso ajudaria quem está tentando adivinhar.
        super("Código inválido ou expirado.");
    }

}
