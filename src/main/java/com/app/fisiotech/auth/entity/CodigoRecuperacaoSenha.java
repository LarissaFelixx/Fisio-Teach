package com.app.fisiotech.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Table(
        name = "codigos_recuperacao_senha",
        indexes = {
                @Index(name = "idx_codigo_recuperacao_email", columnList = "email")
        }
)
public class CodigoRecuperacaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    // Guardamos só o hash (BCrypt) - quem ler o banco não consegue usar o código.
    @Column(name = "codigo_hash", nullable = false, length = 255)
    private String codigoHash;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "tentativas", nullable = false)
    private int tentativas;

    @Column(name = "usado", nullable = false)
    private boolean usado;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    protected CodigoRecuperacaoSenha() {
    }

    public CodigoRecuperacaoSenha(String email, String codigoHash, LocalDateTime dataCriacao, LocalDateTime expiraEm) {
        this.email = email;
        this.codigoHash = codigoHash;
        this.dataCriacao = dataCriacao;
        this.expiraEm = expiraEm;
    }

    public void registrarTentativaInvalida() {
        this.tentativas++;
    }

    public void marcarComoUsado() {
        this.usado = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodigoRecuperacaoSenha that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
