package com.app.fisiotech.mensagem.entity;

import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.profissional.entity.Profissional;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "mensagens")
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Enumerated(EnumType.STRING)
    @Column(name = "autor", nullable = false, length = 20)
    private AutorMensagem autor;

    @Column(name = "conteudo", nullable = false, length = 2000)
    private String conteudo;

    @Column(name = "data_envio", nullable = false, updatable = false)
    private LocalDateTime dataEnvio;

    protected Mensagem() {
    }

    public Mensagem(Paciente paciente, Profissional profissional, AutorMensagem autor, String conteudo) {
        this.paciente = paciente;
        this.profissional = profissional;
        this.autor = autor;
        this.conteudo = conteudo;
    }

    @PrePersist
    public void prePersist() {
        this.dataEnvio = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mensagem mensagem)) return false;
        return Objects.equals(id, mensagem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
